package org.oopscraft.fintics.etf.collector.bak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.etf.FinticsEtfConfiguration;
import org.oopscraft.fintics.etf.dao.AssetEntity;
import org.oopscraft.fintics.etf.dao.DividendEntity;
import org.oopscraft.fintics.etf.dao.DividendRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsEtfConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DividendCollectorTest extends CoreTestSupport {

    private final DividendCollector dividendCollector;

    private final DividendRepository dividendRepository;

    @Test
    void getUsDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = dividendCollector.getUsDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @Test
    void getKrDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(10);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = dividendCollector.getKrDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @Disabled
    @Test
    void collect() {
        // given
        entityManager.persist(AssetEntity.builder()
                .assetId("US.SPY")
                .market("US")
                .build());
        entityManager.persist(AssetEntity.builder()
                .assetId("KR.069500")
                .market("KR")
                .build());
        entityManager.flush();
        // when
        dividendCollector.collect();
        // then
        List<DividendEntity> dividendEntities = dividendRepository.findAll();
        assertTrue(dividendEntities.size() > 0);
    }

}