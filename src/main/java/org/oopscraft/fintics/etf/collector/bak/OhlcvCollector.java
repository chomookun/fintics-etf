package org.oopscraft.fintics.etf.collector.bak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.etf.dao.AssetEntity;
import org.oopscraft.fintics.etf.dao.AssetRepository;
import org.oopscraft.fintics.etf.dao.OhlcvEntity;
import org.oopscraft.fintics.etf.dao.OhlcvRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OhlcvCollector extends AbstractCollector {

    private static final int INTERVAL = 3_000;

    private final AssetRepository assetRepository;

    private final OhlcvRepository ohlcvRepository;

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
                OhlcvEntity latestOhlcvEntity = ohlcvRepository.findFirstByAssetIdOrderByDateDesc(assetEntity.getAssetId()).orElse(null);
                if (latestOhlcvEntity != null) {
                    dateFrom = latestOhlcvEntity.getDate().plusDays(1);
                }

                // check skip
                if (dateFrom.isAfter(LocalDate.now())) {
                    continue;
                }

                // gets ohlcvs
                Asset asset = Asset.from(assetEntity);
                List<Ohlcv> ohlcvs = switch (asset.getMarket()) {
                    case "US" -> getUsOhlcvs(asset, dateFrom, dateTo);
                    case "KR" -> getKrOhlcvs(asset, dateFrom, dateTo);
                    default -> Collections.emptyList();
                };

                List<OhlcvEntity> ohlcvEntities = ohlcvs.stream()
                        .map(it -> {
                            return OhlcvEntity.builder()
                                    .assetId(it.getAssetId())
                                    .date(it.getDate())
                                    .open(it.getOpen())
                                    .high(it.getHigh())
                                    .low(it.getLow())
                                    .close(it.getClose())
                                    .volume(it.getVolume())
                                    .build();
                        })
                        .collect(Collectors.toList());

                // saves bulk
                saveEntities("ohlcvEntities", ohlcvEntities, transactionManager, ohlcvRepository);

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
     * returns us ohlcvs
     * @param asset asset
     * @param dateFrom date from
     * @param dateTo date to
     * @return ohlcvs
     * @see "https://finance.yahoo.com/quote/TLT/history/?period1=1572158513&period2=1730010926"
     */
    List<Ohlcv> getUsOhlcvs(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
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
        List<Long> timestamps = objectMapper.convertValue(resultNode.path("timestamp"), new TypeReference<>(){});
        JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
        List<BigDecimal> opens = objectMapper.convertValue(quoteNode.path("open"), new TypeReference<>(){});
        List<BigDecimal> highs = objectMapper.convertValue(quoteNode.path("high"), new TypeReference<>(){});
        List<BigDecimal> lows = objectMapper.convertValue(quoteNode.path("low"), new TypeReference<>(){});
        List<BigDecimal> closes = objectMapper.convertValue(quoteNode.path("close"), new TypeReference<>(){});
        List<BigDecimal> volumes = objectMapper.convertValue(quoteNode.path("volume"), new TypeReference<>(){});

        List<Ohlcv> ohlcvs = new ArrayList();
        for (int i = 0; i < timestamps.size(); i ++) {
            long timestamp = timestamps.get(i);
            LocalDate date = Instant.ofEpochSecond(timestamp)
                    .atZone(ZoneId.of("America/New_York"))
                    .toLocalDate();
            BigDecimal open = opens.get(i);
            BigDecimal high = highs.get(i);
            BigDecimal low = lows.get(i);
            BigDecimal close = closes.get(i);
            BigDecimal volume = volumes.get(i);
            Ohlcv ohlcv = Ohlcv.builder()
                    .assetId(asset.getAssetId())
                    .date(date)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(volume)
                    .build();
            ohlcvs.add(ohlcv);
        }
        return ohlcvs;
    }

    /**
     * return kr ohlcvs
     * @param asset asset
     * @param dateFrom date from
     * @param dateTo date to
     * @return ohlcvs
     * @see "https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/etf/BIP_CNTS06033V.xml&menuNo=182#"
     */
    List<Ohlcv> getKrOhlcvs(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06033V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "compstInfoStkDayprcList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfCompstInfoPTask";

        Map<String,String> secInfo = getSecInfo(asset.getSymbol());
        String isin = secInfo.get("ISIN");

        List<Ohlcv> ohlcvs = new ArrayList<>();
        int startPage = 1;
        int endPage = 100;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("MENU_NO","182");
                put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,seach,favorites float_left,search02,search02,link,link,wide,wide,top,");
                put("isin", isin);
                put("RGT_RSN_DTAIL_SORT_CD", "11");
                put("fromDt", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("toDt", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
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

            List<Ohlcv> ohlcvPage = rows.stream()
                    .map(row -> {
                        return Ohlcv.builder()
                                .assetId(asset.getAssetId())
                                .date(LocalDate.parse(row.get("STD_DT"), DateTimeFormatter.BASIC_ISO_DATE))
                                .open(new BigDecimal(row.get("MARTP")))
                                .high(new BigDecimal(row.get("HGPRC")))
                                .low(new BigDecimal(row.get("LWPRC")))
                                .close(new BigDecimal(row.get("CPRI")))
                                .volume(new BigDecimal(row.get("TR_QTY")))
                                .build();
                    })
                    .collect(Collectors.toList());
            ohlcvs.addAll(ohlcvPage);

            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }

        // returns
        return ohlcvs;
    }

}
