package org.oopscraft.fintics.etf.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class AssetSearch {

    private String assetId;

    private String name;

    private String market;

    private BigDecimal dividendYieldMin;

    private BigDecimal dividendYieldMax;

    private Integer dividendFrequencyMin;

    private Integer dividendFrequencyMax;

}
