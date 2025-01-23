package org.chomookun.fintics.etf.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.etf.collector.UsMarketCollector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.etf.FinticsEtfConfiguration;
import org.chomookun.fintics.etf.model.Asset;
import org.chomookun.fintics.etf.model.Dividend;
import org.chomookun.fintics.etf.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = FinticsEtfConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class UsMarketCollectorTest extends CoreTestSupport {

    private final UsMarketCollector usMarketCollector;

    @Disabled
    @Test
    void getAssets() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .name("SPDR S&P 500")
                .build();
        // when
        List<Asset> assets = usMarketCollector.getAssets();
        // then
        log.info("assets:{}", assets);
    }

    @Disabled
    @Test
    void getAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .name("SPDR S&P 500")
                .build();
        // when
        Map<String,String> assetDetail = usMarketCollector.getAssetDetail(asset);
        // then
        log.info("assetDetail:{}", assetDetail);
    }

    @Disabled
    @Test
    void getDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .name("SPDR S&P 500")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = usMarketCollector.getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @Disabled
    @Test
    void getOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .name("SPDR S&P 500")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Ohlcv> ohlcvs = usMarketCollector.getOhlcvs(asset, dateFrom, dateTo);
        // then
        log.info("ohlcvs:{}", ohlcvs);
    }

}