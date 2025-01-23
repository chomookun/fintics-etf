package org.chomookun.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.service.OhlcvService;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.etf.FinticsEtfConfiguration;
import org.chomookun.fintics.etf.dao.OhlcvEntity;
import org.chomookun.fintics.etf.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsEtfConfiguration.class)
@RequiredArgsConstructor
class OhlcvServiceTest extends CoreTestSupport {

    private final OhlcvService ohlcvService;

    @Test
    void getOhlcvs() {
        // given
        String assetId = "test";
        LocalDate dateFrom = LocalDate.now().minusDays(1);
        LocalDate dateTo = LocalDate.now();
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .date(dateFrom)
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> ohlcvs = ohlcvService.getOhlcvs(assetId, dateFrom, dateTo);
        // then
        assertTrue(ohlcvs.size() > 0);
    }

}