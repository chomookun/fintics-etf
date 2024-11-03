package org.oopscraft.fintics.etf.simulate;

import lombok.Builder;
import org.oopscraft.fintics.etf.model.*;
import org.oopscraft.fintics.etf.service.DividendService;
import org.oopscraft.fintics.etf.service.OhlcvService;
import org.oopscraft.fintics.etf.simulate.strategy.*;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Builder
public class SimulateTask implements Callable<Simulate> {

    private final Simulate simulate;

    private final OutputStream outputStream;

    private final SimulateBrokerFactory simulateBrokerFactory;

    @Override
    public Simulate call() throws Exception {
        BigDecimal investAmount = simulate.getInvestAmount();
        LocalDate dateFrom = simulate.getDateFrom();
        LocalDate dateTo = simulate.getDateTo();
        List<SimulateAsset> simulateAssets = simulate.getSimulateAssets();

        // broker
        SimulateBroker simulateBroker = simulateBrokerFactory.getObject(dateFrom, dateTo);
        simulateBroker.deposit(investAmount);

        // loop
        for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
            println(date.toString());
            for (SimulateAsset simulateAsset : simulateAssets) {
                simulateBroker.setDate(date);
                String assetId = simulateAsset.getAssetId();
                List<Ohlcv> ohlcvs = simulateBroker.getOhlcvs(assetId);
                BigDecimal price = simulateBroker.getPrice(assetId);

                // holding weight amount
                BigDecimal holdingWeight = simulateAsset.getHoldingWeight();
                BigDecimal holdingWeightAmount = simulateBroker.getTotalAmount()
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                        .multiply(holdingWeight)
                        .setScale(2, RoundingMode.HALF_UP);

                // calculates buy or sell amount
                BigDecimal currentHoldingQuantity = simulateBroker.getHoldingQuantity(assetId);
                BigDecimal currentHoldingAmount = price
                        .multiply(currentHoldingQuantity);

                // runs strategy
                Strategy strategy = StrategyFactory.getObject("test");
                strategy.setDate(date);
                strategy.setAsset(simulateAsset);
                strategy.setOhlcvs(ohlcvs);
                BigDecimal position = strategy.getPosition();

                // check strategy result
                if (position == null) {
                    continue;
                }

                // apply position
                holdingWeightAmount = holdingWeightAmount
                        .multiply(position)
                        .setScale(2, RoundingMode.HALF_UP);

                // buy
                if (currentHoldingAmount.compareTo(holdingWeightAmount) < 0) {
                    BigDecimal buyAmount = holdingWeightAmount.subtract(currentHoldingAmount);
                    BigDecimal buyQuantity = buyAmount
                            .divide(price, MathContext.DECIMAL32)
                            .setScale(4, RoundingMode.FLOOR);
                    simulateBroker.buyAsset(assetId, buyQuantity);
                }

                // sell
                if (currentHoldingAmount.compareTo(holdingWeightAmount) > 0) {
                    BigDecimal sellAmount = currentHoldingAmount.subtract(holdingWeightAmount);
                    BigDecimal sellQuantity = sellAmount
                            .divide(price, MathContext.DECIMAL32)
                            .setScale(4, RoundingMode.FLOOR);
                    simulateBroker.buyAsset(assetId, sellQuantity);
                }
            }
        }

        // result
        SimulateResult simulateResult = SimulateResult.builder()
                .build();
        simulate.setSimulateResult(simulateResult);
        return simulate;
    }

    void println(String line) {
        if (outputStream != null) {
            line = line + '\n';
            try {
                outputStream.write(line.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignore) {}
        }
    }

    BigDecimal calculateHoldingAmount(BigDecimal totalAmount, BigDecimal holdingWeight) {
        return totalAmount.multiply(holdingWeight)
                .setScale(4, RoundingMode.DOWN);
    }

    BigDecimal calculateQuantity(BigDecimal totalAmount, BigDecimal holdingWeight, BigDecimal price) {
        BigDecimal amount = totalAmount.multiply(holdingWeight);
        return amount.divide(price, MathContext.DECIMAL32)
                .setScale(0, RoundingMode.DOWN);
    }

}
