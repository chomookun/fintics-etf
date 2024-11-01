package org.oopscraft.fintics.etf.model;

import lombok.*;
import org.oopscraft.fintics.etf.dao.DividendEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Dividend {

    private String assetId;

    private LocalDate date;

    private BigDecimal amount;

    public static Dividend from(DividendEntity dividendEntity) {
        return Dividend.builder()
                .assetId(dividendEntity.getAssetId())
                .date(dividendEntity.getDate())
                .amount(dividendEntity.getAmount())
                .build();
    }

}
