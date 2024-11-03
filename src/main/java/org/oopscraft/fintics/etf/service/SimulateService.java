package org.oopscraft.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.etf.model.Simulate;
import org.oopscraft.fintics.etf.model.SimulateResult;
import org.oopscraft.fintics.etf.simulate.SimulateTask;
import org.oopscraft.fintics.etf.simulate.SimulateTaskFactory;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@Service
@RequiredArgsConstructor
public class SimulateService {

    private final SimulateTaskFactory simulateTaskFactory;

    public Simulate launchSimulate(Simulate simulate, OutputStream outputStream) {
        SimulateTask simulateTask = simulateTaskFactory.getObject(simulate, outputStream);
        try {
            simulate = simulateTask.call();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return simulate;
    }

}
