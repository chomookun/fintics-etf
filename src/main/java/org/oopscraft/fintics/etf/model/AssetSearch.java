package org.oopscraft.fintics.etf.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class AssetSearch {

    private String assetId;

    private String name;

}
