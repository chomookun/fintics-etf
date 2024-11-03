package org.oopscraft.fintics.etf.simulate;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.etf.model.Simulate;
import org.oopscraft.fintics.etf.service.DividendService;
import org.oopscraft.fintics.etf.service.OhlcvService;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
@RequiredArgsConstructor
public class SimulateTaskFactory {

    private final SimulateBrokerFactory simulateBrokerFactory;

    public SimulateTask getObject(Simulate simulate, OutputStream outputStream) {
        return SimulateTask.builder()
                .simulate(simulate)
                .outputStream(outputStream)
                .simulateBrokerFactory(simulateBrokerFactory)
                .build();
    }

}
