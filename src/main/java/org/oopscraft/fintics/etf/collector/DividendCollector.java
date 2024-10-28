package org.oopscraft.fintics.etf.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.etf.dao.AssetEntity;
import org.oopscraft.fintics.etf.dao.AssetRepository;
import org.oopscraft.fintics.etf.dao.DividendEntity;
import org.oopscraft.fintics.etf.dao.DividendRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DividendCollector extends AbstractCollector {

    private static final int INTERVAL = 3_000;

    private final AssetRepository assetRepository;

    private final DividendRepository dividendRepository;

    private final ObjectMapper objectMapper;

    private final PlatformTransactionManager transactionManager;

    @Override
    public void collect() {
        List<AssetEntity> assetEntities = assetRepository.findAll();
        for (AssetEntity assetEntity : assetEntities) {
            try {
                // defines date from, date to
                LocalDate dateFrom = LocalDate.now().minusYears(20);
                LocalDate dateTo = LocalDate.now();
                DividendEntity latestDividendEntity = dividendRepository.findFirstByAssetIdOrderByDateDesc(assetEntity.getAssetId()).orElse(null);
                if (latestDividendEntity != null) {
                    dateFrom = latestDividendEntity.getDate().plusDays(1);
                }

                // check skip
                if (dateFrom.isAfter(LocalDate.now())) {
                    continue;
                }

                // get dividend histories
                Asset asset = Asset.from(assetEntity);
                List<Dividend> dividends = switch (asset.getMarket()) {
                    case "US" -> getUsDividends(asset, dateFrom, dateTo);
                    case "KR" -> getKrDividends(asset, dateFrom, dateTo);
                    default -> Collections.emptyList();
                };

                // convert to dividend entities
                List<DividendEntity> dividendEntities = dividends.stream()
                        .map(it -> {
                            return DividendEntity.builder()
                                    .assetId(it.getAssetId())
                                    .date(it.getDate())
                                    .amount(it.getAmount())
                                    .build();
                        })
                        .collect(Collectors.toList());

                // saves bulk
                saveEntities("dividendEntities", dividendEntities, transactionManager, dividendRepository);

                // updates dividend interval
                List<DividendEntity> yearlyDividends = dividendRepository.findAllBy(asset.getAssetId(), LocalDate.now().minusYears(1), LocalDate.now()).stream()
                        .filter(it -> it.getDate().isAfter(LocalDate.now().minusYears(1)))
                        .toList();
                long yearlyPaymentCount = yearlyDividends.size();
                String dividendFrequency = null;
                if (yearlyPaymentCount > 0) {
                    if (yearlyPaymentCount >= 10) {
                        dividendFrequency = "MONTHLY";
                    } else if (yearlyPaymentCount >= 3) {
                        dividendFrequency = "QUARTERLY";
                    } else if (yearlyPaymentCount >= 1) {
                        dividendFrequency = "YEARLY";
                    }
                    assetEntity.setDividendFrequency(dividendFrequency);
                }

                // updates dividend yield
                BigDecimal dividendAmount = yearlyDividends.stream()
                        .map(DividendEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (dividendAmount.compareTo(BigDecimal.ZERO) > 0 && assetEntity.getClose() != null) {
                    BigDecimal dividendYield = dividendAmount
                            .divide(assetEntity.getClose(), MathContext.DECIMAL32)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.FLOOR);
                    assetEntity.setDividendYield(dividendYield);
                }

                // saves asset entity
                assetEntity.setUpdatedDate(LocalDate.now());
                saveEntities("assetEntities", List.of(assetEntity), transactionManager, assetRepository);

            } catch (Throwable t) {
                log.warn(t.getMessage());
            } finally {
                try {
                    Thread.sleep(INTERVAL);
                } catch (Throwable ignore) {}
            }
        }
    }

    /**
     * returns us dividends
     * @param asset asset
     * @return list of dividend
     */
    List<Dividend> getUsDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = createYahooHeader();

        // url
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", asset.getSymbol());
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("events", "events=capitalGain|div|split")
                .queryParam("interval", "1d")
                .queryParam("period1", dateFrom.atStartOfDay().atOffset(ZoneOffset.UTC).toEpochSecond())
                .queryParam("period2", dateTo.atStartOfDay().atOffset(ZoneOffset.UTC).toEpochSecond())
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode resultNode = rootNode.path("chart").path("result").get(0);
        JsonNode eventsNode = resultNode.path("events").path("dividends");
        Map<String,Map<String,Double>> dividendsMap = objectMapper.convertValue(eventsNode, new TypeReference<>(){});

        List<Dividend> dividends = new ArrayList<>();
        for (Map.Entry<String, Map<String,Double>> entry : dividendsMap.entrySet()) {
            Map<String,Double> value = entry.getValue();
            LocalDate date = Instant.ofEpochSecond(value.get("date").longValue())
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDate();
            BigDecimal amount = BigDecimal.valueOf(value.get("amount"));
            Dividend dividend = Dividend.builder()
                    .assetId(asset.getAssetId())
                    .date(date)
                    .amount(amount)
                    .build();
            dividends.add(dividend);
        }

        // returns
        return dividends;
    }

    List<Dividend> getKrDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06030V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "exerInfoDtramtPayStatPlist";
        String task = "ksd.safe.bip.cnts.etf.process.EtfExerInfoPTask";

        Map<String,String> secInfo = getSecInfo(asset.getSymbol());
        String isin = secInfo.get("ISIN");

        List<Dividend> dividends = new ArrayList<>();
        int startPage = 1;
        int endPage = 30;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("MENU_NO","179");
                put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,searchIcon,seach,searchIcon,seach,link,link,wide,wide,top,");
                put("isin", isin);
                put("RGT_RSN_DTAIL_SORT_CD", "11");
                put("fromRGT_STD_DT", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("toRGT_STD_DT", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("START_PAGE", String.valueOf(finalStartPage));
                put("END_PAGE", String.valueOf(finalEndPage));
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);

            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);

            String responseBody = responseEntity.getBody();
            List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

            List<Dividend> dividendPage = rows.stream()
                    .map(row -> {
                        return Dividend.builder()
                                .assetId(asset.getAssetId())
                                .date(LocalDate.parse(row.get("RGT_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE))
                                .amount(new BigDecimal(row.get("ESTM_STDPRC")))
                                .build();
                    })
                    .collect(Collectors.toList());
            dividends.addAll(dividendPage);

            // check next page
            if (rows.size() < 30) {
                break;
            } else {
                startPage += 30;
                endPage += 30;
            }
        }

        // returns
        return dividends;
    }




}
