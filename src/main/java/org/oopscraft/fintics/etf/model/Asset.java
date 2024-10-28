package org.oopscraft.fintics.etf.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.etf.dao.AssetEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    private BigDecimal marketCap;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal dividendYield;

    private String dividendFrequency;

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

    /**
     * get asset icon
     * @return icon url
     */
    public String getIcon() {
        return IconFactory.getIcon(this);
    }

    /**
     * gets asset link
     * @return link url
     */
    public List<Link> getLinks() {
        return LinkFactory.getLinks(this);
    }

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .name(assetEntity.getName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .updatedDate(assetEntity.getUpdatedDate())
                .marketCap(assetEntity.getMarketCap())
                .close(assetEntity.getClose())
                .volume(assetEntity.getVolume())
                .dividendYield(assetEntity.getDividendYield())
                .dividendFrequency(assetEntity.getDividendFrequency())
                .build();
    }

}
