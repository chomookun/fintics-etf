package org.oopscraft.fintics.etf.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class SimulateResult {

    private BigDecimal dividendYield;

    private BigDecimal dividendYieldAmount;

    private BigDecimal capitalGain;

    private BigDecimal capitalGainAmount;

    private BigDecimal totalReturn;

    private BigDecimal totalReturnAount;

}
