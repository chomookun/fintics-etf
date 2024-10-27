package org.oopscraft.fintics.etf.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.etf.model.Dividend;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
public class DividendResponse {

    private String assetId;

    private LocalDate date;

    private BigDecimal amount;

    public static DividendResponse from(Dividend dividend) {
        return DividendResponse.builder()
                .assetId(dividend.getAssetId())
                .date(dividend.getDate())
                .amount(dividend.getAmount())
                .build();
    }

}
