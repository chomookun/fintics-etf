package org.oopscraft.fintics.etf.simulate.strategy;

import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.oopscraft.fintics.etf.model.SimulateAsset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public abstract class Strategy {

    @Setter
    private LocalDate date;

    @Setter
    private SimulateAsset asset;

    @Setter
    private List<Ohlcv> ohlcvs;

    public abstract BigDecimal getPosition();

}
