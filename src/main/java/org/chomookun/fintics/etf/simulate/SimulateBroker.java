package org.chomookun.fintics.etf.simulate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chomookun.fintics.etf.model.Ohlcv;
import org.chomookun.fintics.etf.service.OhlcvService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class SimulateBroker {

    private final LocalDate dateFrom;

    private final LocalDate dateTo;

    private final OhlcvService ohlcvService;

    @Setter
    private LocalDate date;

    @Builder.Default
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Builder.Default
    private final Map<String,List<Ohlcv>> ohlcvsMap = new LinkedHashMap<>();

    @Builder.Default
    private Map<String,BigDecimal> quantityMap = new LinkedHashMap<>();

    public List<Ohlcv> getOhlcvs(String assetId) {
        List<Ohlcv> ohlcvs = ohlcvsMap.get(assetId);
        if (ohlcvs == null) {
            ohlcvs = ohlcvService.getOhlcvs(assetId, dateFrom, dateTo);
            ohlcvsMap.put(assetId, ohlcvs);
        }
        return ohlcvs;
    }

    public void deposit(BigDecimal amount) {
        cashAmount = cashAmount.add(amount);
    }

    public BigDecimal withdraw(BigDecimal amount) {
        if (amount.compareTo(cashAmount) > 0) {
            amount = cashAmount;
        }
        cashAmount = cashAmount.subtract(amount);
        return amount;
    }

    BigDecimal getPrice(String assetId) {
        List<Ohlcv> ohlcvs = getOhlcvs(assetId);
        Ohlcv ohlcv = ohlcvs.stream()
                .filter(it -> it.getDate().isEqual(getDate()))
                .findFirst()
                .orElseThrow();
        return ohlcv.getClose();
    }

    public BigDecimal getTotalAmount() {
        return cashAmount;
    }

    public boolean buyAsset(String assetId, BigDecimal quantity) {
        BigDecimal buyPrice = getPrice(assetId);
        BigDecimal buyAmount = buyPrice.multiply(quantity);
        buyAmount = withdraw(buyAmount);
        quantity = buyAmount.divide(buyPrice, MathContext.DECIMAL32);

        // saves assets
        BigDecimal previousQuantity = quantityMap.getOrDefault(assetId, BigDecimal.ZERO);
        BigDecimal nextQuantity = previousQuantity.add(quantity);
        quantityMap.put(assetId, nextQuantity);
        return true;
    }

    public boolean sellAsset(String assetId, BigDecimal price, BigDecimal quantity) {
        BigDecimal previousQuantity = quantityMap.getOrDefault(assetId, BigDecimal.ZERO);
        // check previous quantity
        if (previousQuantity.compareTo(quantity) < 0) {
            return false;
        }
        // updates asset quantity
        BigDecimal nextQuantity = previousQuantity.subtract(quantity);
        quantityMap.put(assetId, nextQuantity);

        // deposit
        BigDecimal sellAmount = price.multiply(quantity);
        deposit(sellAmount);
        return true;
    }


    public BigDecimal getHoldingQuantity(String assetId) {
        return quantityMap.getOrDefault(assetId, BigDecimal.ZERO);
    }
}
