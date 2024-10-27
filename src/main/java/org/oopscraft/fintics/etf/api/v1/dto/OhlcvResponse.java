package org.oopscraft.fintics.etf.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.etf.model.Ohlcv;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
public class OhlcvResponse {

    private String assetId;

    private LocalDate date;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private boolean interpolated;

    public static OhlcvResponse from(Ohlcv ohlcv) {
        return OhlcvResponse.builder()
                .assetId(ohlcv.getAssetId())
                .date(ohlcv.getDate())
                .open(ohlcv.getOpen())
                .high(ohlcv.getHigh())
                .low(ohlcv.getLow())
                .close(ohlcv.getClose())
                .volume(ohlcv.getVolume())
                .interpolated(ohlcv.isInterpolated())
                .build();
    }
}
