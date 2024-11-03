package org.oopscraft.fintics.etf.simulate.strategy;


import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class StrategyResult {

    private Action action;

    private BigDecimal position;

    private String message;

    StrategyResult of(Action action, BigDecimal position, String message) {
        return StrategyResult.builder()
                .action(action)
                .position(position)
                .message(message)
                .build();
    }

    enum Action {
        BUY, SELL
    }

}
