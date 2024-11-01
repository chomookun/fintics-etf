package org.oopscraft.fintics.etf.model;

import lombok.*;
import org.oopscraft.fintics.etf.dao.OhlcvEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

    private String assetId;

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private boolean interpolated;

    public static Ohlcv from(OhlcvEntity ohlcvEntity) {
        return Ohlcv.builder()
                .assetId(ohlcvEntity.getAssetId())
                .date(ohlcvEntity.getDate())
                .open(ohlcvEntity.getOpen())
                .high(ohlcvEntity.getHigh())
                .low(ohlcvEntity.getLow())
                .close(ohlcvEntity.getClose())
                .volume(ohlcvEntity.getVolume())
                .build();
    }

}
