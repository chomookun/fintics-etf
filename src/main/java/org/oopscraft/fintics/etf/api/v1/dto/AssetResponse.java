package org.oopscraft.fintics.etf.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.etf.model.Asset;

import java.math.BigDecimal;

@Builder
@Getter
public class AssetResponse {

    private String assetId;

    private String name;

    private String market;

    private String exchange;

    private String type;

    private BigDecimal marketCap;

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .name(asset.getName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .type(asset.getType())
                .marketCap(asset.getMarketCap())
                .build();
    }

}
