package org.chomookun.fintics.etf.api.v1.dto;

import lombok.*;
import org.chomookun.fintics.etf.model.SimulateResult;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimulateResponse {

    private BigDecimal investAmount;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    @Setter
    private SimulateResult simulateResult;

}
