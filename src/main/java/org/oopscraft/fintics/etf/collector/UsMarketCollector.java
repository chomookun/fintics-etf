package org.oopscraft.fintics.etf.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oopscraft.fintics.etf.dao.AssetRepository;
import org.oopscraft.fintics.etf.dao.DividendRepository;
import org.oopscraft.fintics.etf.dao.OhlcvRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.http.HttpHeaders;
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UsMarketCollector extends AbstractMarketCollector {

    private final ObjectMapper objectMapper;

    public UsMarketCollector(
            PlatformTransactionManager transactionManager,
            AssetRepository assetRepository,
            DividendRepository dividendRepository,
            OhlcvRepository ohlcvRepository,
            ObjectMapper objectMapper
    ) {
        super(transactionManager, assetRepository, dividendRepository, ohlcvRepository);
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 60_000 * 60 * 24)
    void schedule() {
        collect();
    }

    @Override
    List<Asset> getAssets() {
        String url = "https://api.nasdaq.com/api/screener/etf?download=true";
        RequestEntity<Void> requestEntity = RequestEntity.get(url)
                .headers(createNasdaqHeaders())
                .build();
        sleep();
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

    @Override
    Map<String, String> getAssetDetail(Asset asset) {
        Map<String,String> assetDetail = new LinkedHashMap<>();

        // calls summary api
        HttpHeaders headers = createNasdaqHeaders();
        String summaryUrl = String.format(
                "https://api.nasdaq.com/api/quote/%s/summary?assetclass=etf",
                asset.getSymbol()
        );
        RequestEntity<Void> summaryRequestEntity = RequestEntity.get(summaryUrl)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> summaryResponseEntity = getRestTemplate().exchange(summaryRequestEntity, String.class);
        JsonNode summaryRootNode;
        try {
            summaryRootNode = objectMapper.readTree(summaryResponseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode summaryDataNode = summaryRootNode.path("data").path("summaryData");
        HashMap<String, Map<String, String>> summaryDataMap = objectMapper.convertValue(summaryDataNode, new TypeReference<>() {
        });

        // price, market cap
        BigDecimal close = null;
        BigDecimal volume = null;
        BigDecimal marketCap = null;
        BigDecimal dividendYield = BigDecimal.ZERO;
        for (String name : summaryDataMap.keySet()) {
            Map<String, String> map = summaryDataMap.get(name);
            String value = map.get("value");
            if (Objects.equals(name, "PreviousClose")) {
                close = convertCurrencyToNumber(value, Currency.getInstance("USD"));
            }
            if (Objects.equals(name, "AvgDailyVol20Days")) {
                volume = convertStringToNumber(value);
            }
            if (Objects.equals(name, "MarketCap")) {
                marketCap = convertStringToNumber(value);
            }
            if (Objects.equals(name, "Yield")) {
                dividendYield = convertPercentageToNumber(value);
            }
        }
        assetDetail.put("close", close.toPlainString());
        assetDetail.put("volume", volume.toPlainString());
        assetDetail.put("marketCap", marketCap.toPlainString());
        assetDetail.put("dividendYield", dividendYield.toPlainString());

        // dividend frequency
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
        List<Dividend> dividends = getDividends(asset, dateFrom, dateTo);
        if (dividends.size() > 0) {
            Integer dividendFrequency = dividends.size();
            assetDetail.put("dividendFrequency", String.valueOf(dividendFrequency));
        }

        // capital gain
        LocalDate ohlcvDateFrom = LocalDate.now().minusYears(1);
        LocalDate ohlcvDateTo = LocalDate.now();
        List<Ohlcv> ohlcvs = getOhlcvs(asset, ohlcvDateFrom, ohlcvDateTo);
        BigDecimal startClose = ohlcvs.get(ohlcvs.size() - 1).getClose();
        BigDecimal endClose = ohlcvs.get(0).getClose();
        BigDecimal capitalGain = endClose.subtract(startClose)
                .divide(startClose, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.FLOOR);
        assetDetail.put("capitalGain", capitalGain.toPlainString());

        // total return
        BigDecimal totalReturn = capitalGain.add(dividendYield)
                .setScale(2, RoundingMode.FLOOR);
        assetDetail.put("totalReturn", totalReturn.toPlainString());

        // returns
        return assetDetail;
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
                sleep();
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

    @Override
    List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = createYahooHeader();
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
        sleep();
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
        if (dividendsMap != null) {
            for (Map.Entry<String, Map<String, Double>> entry : dividendsMap.entrySet()) {
                Map<String, Double> value = entry.getValue();
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
        }

        // sort date descending
        dividends.sort(Comparator
                .comparing(Dividend::getDate)
                .reversed());

        // returns
        return dividends;
    }

    @Override
    List<Ohlcv> getOhlcvs(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = createYahooHeader();
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
        sleep();
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

        // sort date descending
        ohlcvs.sort(Comparator
                .comparing(Ohlcv::getDate)
                .reversed());

        // returns
        return ohlcvs;
    }

    /**
     * creates yahoo finance http headers
     * @return http headers
     */
    HttpHeaders createYahooHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority"," query1.finance.yahoo.com");
        headers.add("Accept", "*/*");
        headers.add("origin", "https://finance.yahoo.com");
        headers.add("referer", "");
        headers.add("Sec-Ch-Ua","\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"");
        headers.add("Sec-Ch-Ua-Mobile","?0");
        headers.add("Sec-Ch-Ua-Platform", "macOS");
        headers.add("Sec-Fetch-Dest","document");
        headers.add("Sec-Fetch-Mode","navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * creates nasdaq http headers
     * @return http headers
     */
    HttpHeaders createNasdaqHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority","api.nasdaq.com");
        headers.add("origin","https://www.nasdaq.com");
        headers.add("referer","https://www.nasdaq.com");
        headers.add("sec-ch-ua","\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"");
        headers.add("sec-ch-ua-mobile","?0");
        headers.add("sec-ch-ua-platform", "macOS");
        headers.add("sec-fetch-dest","empty");
        headers.add("sec-fetch-mode","cors");
        headers.add("sec-fetch-site", "same-site");
        headers.add("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }

}
