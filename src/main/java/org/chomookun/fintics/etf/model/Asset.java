package org.chomookun.fintics.etf.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.etf.dao.AssetEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    private String assetId;

    private String name;

    private String market;

    private String exchange;

    private LocalDate updatedDate;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal marketCap;

    private Integer dividendFrequency;

    private BigDecimal dividendYield;

    private BigDecimal capitalGain;

    private BigDecimal totalReturn;

    /**
     * gets symbol
     * @return symbol
     */
    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .name(assetEntity.getName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .updatedDate(assetEntity.getUpdatedDate())
                .close(assetEntity.getClose())
                .volume(assetEntity.getVolume())
                .marketCap(assetEntity.getMarketCap())
                .dividendFrequency(assetEntity.getDividendFrequency())
                .dividendYield(assetEntity.getDividendYield())
                .capitalGain(assetEntity.getCapitalGain())
                .totalReturn(assetEntity.getTotalReturn())
                .build();
    }

}
