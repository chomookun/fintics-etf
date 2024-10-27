package org.oopscraft.fintics.etf.collector;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.etf.FinticsEtfConfiguration;
import org.oopscraft.fintics.etf.dao.AssetEntity;
import org.oopscraft.fintics.etf.dao.OhlcvEntity;
import org.oopscraft.fintics.etf.dao.OhlcvRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsEtfConfiguration.class)
@RequiredArgsConstructor
class OhlcvCollectorTest extends CoreTestSupport {

    private final OhlcvCollector ohlcvCollector;

    private final OhlcvRepository ohlcvRepository;

    @Test
    void getUsOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .market("US")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Ohlcv> ohlcvs = ohlcvCollector.getUsOhlcvs(asset, dateFrom, dateTo);
        // then
        assertTrue(ohlcvs.size() > 0);
    }

    @Test
    void getKrOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .market("KR")
                .build();
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Ohlcv> ohlcvs = ohlcvCollector.getKrOhlcvs(asset, dateFrom, dateTo);
        // then
        assertTrue(ohlcvs.size() > 0);
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
        ohlcvCollector.collect();
        // then
        List<OhlcvEntity> ohlcvEntities = ohlcvRepository.findAll();
        assertTrue(ohlcvEntities.size() > 0);
    }

}