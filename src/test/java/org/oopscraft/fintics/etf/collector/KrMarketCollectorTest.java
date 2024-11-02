package org.oopscraft.fintics.etf.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.etf.FinticsEtfConfiguration;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = FinticsEtfConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KrMarketCollectorTest extends CoreTestSupport {

    private final KrMarketCollector krMarketCollector;

    @Disabled
    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = krMarketCollector.getAssets();
        // then
        log.info("assets: {}", assets);
    }

    @Disabled
    @Test
    void getAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                // close, volume, marketCap 은 목록 조회 시 존재 하므로 재조회 없이 목록의 값을 사용함.
                .close(BigDecimal.valueOf(30_000))
                .volume(BigDecimal.valueOf(10_000))
                .marketCap(BigDecimal.valueOf(1_000_000))
                .build();
        // when
        Map<String,String> assetDetail = krMarketCollector.getAssetDetail(asset);
        // then
        log.info("assetDetail:{}", assetDetail);
    }

    @Disabled
    @Test
    void getDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(10);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = krMarketCollector.getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @Disabled
    @Test
    void getOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(10);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Ohlcv> ohlcvs = krMarketCollector.getOhlcvs(asset, dateFrom, dateTo);
        // then
        log.info("ohlcvs: {}", ohlcvs);
    }

}