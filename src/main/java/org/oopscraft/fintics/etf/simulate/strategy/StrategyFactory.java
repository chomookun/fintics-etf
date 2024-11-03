package org.oopscraft.fintics.etf.simulate.strategy;

import org.springframework.stereotype.Component;

public class StrategyFactory {

    public static Strategy getObject(String strategyName) {
        // todo
        return new RebalanceMonthlyStrategy();
    }

}
