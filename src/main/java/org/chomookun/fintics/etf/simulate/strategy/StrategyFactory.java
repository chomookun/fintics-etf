package org.chomookun.fintics.etf.simulate.strategy;

public class StrategyFactory {

    public static Strategy getObject(String strategyName) {
        // todo
        return new RebalanceMonthlyStrategy();
    }

}
