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

    private BigDecimal marketCap;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal dividendYield;

    private String dividendFrequency;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .symbol(asset.getSymbol())
                .icon(asset.getIcon())
                .name(asset.getName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .updatedDate(asset.getUpdatedDate())
                .marketCap(asset.getMarketCap())
                .close(asset.getClose())
                .volume(asset.getVolume())
                .dividendYield(asset.getDividendYield())
                .dividendFrequency(asset.getDividendFrequency())
                .icon(asset.getIcon())
                .links(asset.getLinks().stream().map(LinkResponse::from).toList())
                .build();
    }

}
