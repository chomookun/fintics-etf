package org.oopscraft.fintics.etf.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oopscraft.fintics.etf.dao.AssetEntity;
import org.oopscraft.fintics.etf.dao.AssetRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractCollector {

    private final PlatformTransactionManager transactionManager;

    private final ObjectMapper objectMapper;

    private final AssetRepository assetRepository;

    @Override
    public void collect() {
        try {
            List<AssetEntity> assetEntities = assetRepository.findAll();
            List<Asset> assets = new ArrayList<>();
            assets.addAll(getUsAssets());
            assets.addAll(getKrAssets());
            for (Asset asset : assets) {
                AssetEntity assetEntity = assetEntities.stream()
                        .filter(it -> Objects.equals(it.getAssetId(), asset.getAssetId()))
                        .findFirst()
                        .orElse(null);
                if (assetEntity == null) {
                    assetEntity = AssetEntity.builder()
                            .assetId(asset.getAssetId())
                            .build();
                    assetEntities.add(assetEntity);
                }
                assetEntity.setUpdatedDate(LocalDate.now());
                assetEntity.setName(asset.getName());
                assetEntity.setMarket(asset.getMarket());
                assetEntity.setExchange(asset.getExchange());
                assetEntity.setMarketCap(asset.getMarketCap());
                assetEntity.setClose(asset.getClose());
                assetEntity.setVolume(asset.getVolume());
            }

            saveEntities("assetEntities", assetEntities, transactionManager, assetRepository);
        } catch (Throwable t) {
            log.warn(t.getMessage());
        }
    }

    /**
     * gets list of ETF
     * @see [Nasdaq Symbol Screener](https://www.nasdaq.com/market-activity/etf/screener)
     * @return list of etf asset
     */
    public List<Asset> getUsAssets() {
        String url = "https://api.nasdaq.com/api/screener/etf?download=true";
        RequestEntity<Void> requestEntity = RequestEntity.get(url)
                .headers(createNasdaqHeaders())
                .build();
        ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode rowsNode = rootNode.path("data").path("data").path("rows");
        List<Map<String, String>> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1LastSalePrice = new BigDecimal(StringUtils.defaultIfBlank(o1.get("lastSalePrice"),"$0").replace("$",""));
            BigDecimal o2LastSalePrice = new BigDecimal(StringUtils.defaultIfBlank(o2.get("lastSalePrice"),"$0").replace("$",""));
            return o2LastSalePrice.compareTo(o1LastSalePrice);
        });

        List<Asset> assets = rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId("US", row.get("symbol")))
                        .name(row.get("companyName"))
                        .market("US")
                        .build())
                .collect(Collectors.toList());

        // fill exchange
        List<String> symbols = assets.stream().map(Asset::getSymbol).toList();
        Map<String, String> exchangeMap = getUsExchangeMap(symbols);
        assets.forEach(asset -> asset.setExchange(exchangeMap.get(asset.getSymbol())));

        // return
        return assets;
    }

    /**
     * gets exchange map
     * @param symbols list of symbols to retrieve
     * @return exchange map
     */
    Map<String, String> getUsExchangeMap(List<String> symbols) {
        Map<String, String> exchangeMicMap = new LinkedHashMap<>();
        final int BATCH_SIZE = 100;
        try {
            HttpHeaders headers = createYahooHeader();
            for (int i = 0; i < symbols.size(); i += BATCH_SIZE) {
                List<String> batchSymbols = symbols.subList(i, Math.min(i + BATCH_SIZE, symbols.size()));
                String symbolParam = String.join(",", batchSymbols);
                String url = String.format("https://query2.finance.yahoo.com/v1/finance/quoteType/?symbol=%s&lang=en-US&region=US", symbolParam);
                RequestEntity<Void> requestEntity = RequestEntity.get(url)
                        .headers(headers)
                        .build();
                String responseBody = getRestTemplate().exchange(requestEntity, String.class).getBody();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultNode = rootNode.path("quoteType").path("result");
                List<Map<String, String>> results = objectMapper.convertValue(resultNode, new TypeReference<>() {});
                for (Map<String, String> result : results) {
                    String symbol = result.get("symbol");
                    String exchange = result.get("exchange");
                    String exchangeMic = switch (exchange) {
                        case "NGM" -> "XNAS";
                        case "PCX" -> "XASE";
                        // BATS Exchange to BATS (currently Cboe BZX Exchange)
                        case "BTS" -> "BATS";
                        default -> "XNYS";
                    };
                    exchangeMicMap.put(symbol, exchangeMic);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching exchange information", e);
        }
        // return
        return exchangeMicMap;
    }

    public List<Asset> getKrAssets() {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06025V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "secnIssuStatPList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfSetredInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","174");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,detail,seach,searchIcon,comparison,link,link,wide,wide,top,");
            put("mngco", "");
            put("SETUP_DT", "");
            put("from_TOT_RECM_RATE", "");
            put("to_TOT_RECM_RATE", "");
            put("from_NETASST_TOTAMT", "");
            put("to_NETASST_TOTAMT", "");
            put("kor_SECN_NM", "");
            put("ic4_select", "2");
            put("select_sorting", "2");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String exchange = "XKRX";

        // convert assets
        return rows.stream()
                .map(row -> {
                    // market cap (etf is 1 krw unit)
                    BigDecimal marketCap = toNumber(row.get("NETASST_TOTAMT"), null);
                    if(marketCap != null) {
                        marketCap = marketCap.divide(BigDecimal.valueOf(100_000_000), MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                    }

                    // return
                    return Asset.builder()
                            .assetId(toAssetId("KR", row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market("KR")
                            .exchange("KRX")
                            .marketCap(marketCap)
                            .close(new BigDecimal(row.get("CPRI")))
                            .volume(new BigDecimal(row.get("TR_QTY")))
                            .build();
                })
                .collect(Collectors.toList());
    }

}
