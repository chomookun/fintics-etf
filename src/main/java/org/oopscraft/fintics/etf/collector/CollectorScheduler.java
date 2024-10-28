package org.oopscraft.fintics.etf.collector;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectorScheduler {

    private final AssetCollector assetCollector;

    private final DividendCollector dividendCollector;

    private final OhlcvCollector ohlcvCollector;

    @Scheduled(initialDelay = 10_000, fixedDelay = 3_600_000 * 24)
    public void collect() {
        assetCollector.collect();
        dividendCollector.collect();
        ohlcvCollector.collect();
    }

}
