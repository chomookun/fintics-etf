package org.oopscraft.fintics.etf.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.etf.model.Asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class AssetResponse {

    private String assetId;

    private String symbol;

    private String icon;

    private String name;

    private String market;

    private String exchange;

    private LocalDate updatedDate;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal marketCap;

    private BigDecimal dividendYield;

    private Integer dividendFrequency;

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
    public List<LinkResponse> getLinks() {
        return LinkResponseFactory.getLinks(this);
    }

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .updatedDate(asset.getUpdatedDate())
                .close(asset.getClose())
                .volume(asset.getVolume())
                .marketCap(asset.getMarketCap())
                .dividendYield(asset.getDividendYield())
                .dividendFrequency(asset.getDividendFrequency())
                .build();
    }

}
