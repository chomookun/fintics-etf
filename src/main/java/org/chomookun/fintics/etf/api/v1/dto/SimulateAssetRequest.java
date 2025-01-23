package org.chomookun.fintics.etf.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimulateAssetRequest {

    private String assetId;

    private BigDecimal holdingWeight;

}
