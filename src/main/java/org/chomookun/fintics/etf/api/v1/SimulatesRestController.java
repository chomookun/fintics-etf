package org.chomookun.fintics.etf.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.api.v1.dto.SimulateRequest;
import org.chomookun.fintics.etf.model.Simulate;
import org.chomookun.fintics.etf.model.SimulateAsset;
import org.chomookun.fintics.etf.service.SimulateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/simulates")
@RequiredArgsConstructor
public class SimulatesRestController {

    private final SimulateService simulateService;

    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<StreamingResponseBody> launchSimulate(@RequestBody SimulateRequest simulateRequest) {
        StreamingResponseBody stream = outputStream -> {
            Simulate simulate = Simulate.builder()
                    .investAmount(simulateRequest.getInvestAmount())
                    .dateFrom(simulateRequest.getDateFrom())
                    .dateTo(simulateRequest.getDateTo())
                    .simulateAssets(simulateRequest.getSimulateAssets().stream()
                            .map(it -> SimulateAsset.builder()
                                    .assetId(it.getAssetId())
                                    .holdingWeight(it.getHoldingWeight())
                                    .build()).collect(Collectors.toList()))
                    .build();
            simulate = simulateService.launchSimulate(simulate, outputStream);
            String simulateJson = objectMapper.writeValueAsString(simulate);
            outputStream.write(simulateJson.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        };
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(stream);
    }

}
