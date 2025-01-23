package org.chomookun.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.service.DividendService;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.etf.FinticsEtfApplication;
import org.chomookun.fintics.etf.dao.DividendEntity;
import org.chomookun.fintics.etf.model.Dividend;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsEtfApplication.class)
@RequiredArgsConstructor
class DividendServiceTest extends CoreTestSupport {

    private final DividendService dividendService;

    @Test
    void getDividends() {
        // given
        String assetId = "test";
        LocalDate dateFrom = LocalDate.now().minusWeeks(1);
        LocalDate dateTo = LocalDate.now();
        DividendEntity dividendEntity = DividendEntity.builder()
                .assetId(assetId)
                .date(dateTo)
                .amount(BigDecimal.valueOf(100))
                .build();
        entityManager.persist(dividendEntity);
        entityManager.flush();
        // when
        List<Dividend> dividends = dividendService.getDividends(assetId, dateFrom, dateTo);
        // then
        assertTrue(dividends.size() > 0);
    }

}