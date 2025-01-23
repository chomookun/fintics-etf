package org.chomookun.fintics.etf.simulate;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.model.Simulate;
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
