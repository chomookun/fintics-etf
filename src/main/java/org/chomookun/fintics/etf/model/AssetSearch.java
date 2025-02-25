package org.chomookun.fintics.etf.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AssetSearch {

    private String assetId;

    private String name;

    private String market;

    private Integer dividendFrequencyMin;

    private Integer dividendFrequencyMax;

}
