package org.oopscraft.fintics.etf.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SimulateAsset {

    private String assetId;

    private BigDecimal holdingWeight;

}
