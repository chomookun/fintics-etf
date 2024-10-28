package org.oopscraft.fintics.etf.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Simulate {

    private BigDecimal investAmount;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    @Builder.Default
    private List<SimulateAsset> simulateAssets = new ArrayList<>();

}
