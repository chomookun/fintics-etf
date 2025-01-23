package org.chomookun.fintics.etf.simulate;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.service.OhlcvService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SimulateBrokerFactory {

    private final OhlcvService ohlcvService;

    public SimulateBroker getObject(LocalDate dateFrom, LocalDate dateTo) {
        return SimulateBroker.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .ohlcvService(ohlcvService)
                .build();
    }

}
