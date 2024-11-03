package org.oopscraft.fintics.etf.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.etf.model.SimulateAsset;
import org.oopscraft.fintics.etf.model.SimulateResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimulateRequest {

    private BigDecimal investAmount;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    @Builder.Default
    private List<SimulateAssetRequest> simulateAssets = new ArrayList<>();

    @Setter
    private SimulateResult simulateResult;

}
