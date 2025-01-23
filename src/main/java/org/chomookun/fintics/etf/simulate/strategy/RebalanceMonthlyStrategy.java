package org.chomookun.fintics.etf.simulate.strategy;

import java.math.BigDecimal;

public class RebalanceMonthlyStrategy extends Strategy {

    @Override
    public BigDecimal getPosition() {
        if (getDate().getDayOfMonth() != 1) {
            return null;
        }
        return BigDecimal.ONE;
    }

}
