package org.oopscraft.fintics.emp.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.emp.FinticsEmpConfiguration;
import org.oopscraft.fintics.emp.model.Asset;
import org.oopscraft.fintics.emp.model.Dividend;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsEmpConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DividendCollectorTest extends CoreTestSupport {

    private final DividendCollector dividendCollector;

    @Test
    void getUsDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .build();
        LocalDate dateTo = LocalDate.now();
        LocalDate dateFrom = dateTo.minusYears(1);
        // when
        List<Dividend> dividends = dividendCollector.getUsDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

}