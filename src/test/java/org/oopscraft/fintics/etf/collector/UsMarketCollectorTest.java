package org.oopscraft.fintics.etf.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.etf.FinticsEtfConfiguration;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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