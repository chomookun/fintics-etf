package org.oopscraft.fintics.emp.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.analysis.function.Divide;
import org.oopscraft.fintics.emp.model.Asset;
import org.oopscraft.fintics.emp.model.Dividend;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DividendCollector extends AbstractCollector {

    private final ObjectMapper objectMapper;

    @Scheduled(initialDelay = 600_000, fixedDelay = 60_000 * 60 * 24)
    public void collect() {

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

    /* creates yahoo http header
     * @return http headers
     */
    private HttpHeaders createYahooHeader() {
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


    List<Dividend> getKrDividends(Asset asset) {

        return new ArrayList<>();
    }




}
