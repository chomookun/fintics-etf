package org.chomookun.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.model.Simulate;
import org.chomookun.fintics.etf.simulate.SimulateTask;
import org.chomookun.fintics.etf.simulate.SimulateTaskFactory;
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
