package org.oopscraft.fintics.etf.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class SimulateAsset extends Asset {

    private BigDecimal holdingWeight;

}
