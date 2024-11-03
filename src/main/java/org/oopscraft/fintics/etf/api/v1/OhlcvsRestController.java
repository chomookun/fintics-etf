package org.oopscraft.fintics.etf.api.v1;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.etf.api.v1.dto.OhlcvResponse;
import org.oopscraft.fintics.etf.service.OhlcvService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ohlcvs")
@RequiredArgsConstructor
public class OhlcvsRestController {

    private final OhlcvService ohlcvService;

    @GetMapping("{assetId}")
    public ResponseEntity<List<OhlcvResponse>> getOhlcvs(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false)LocalDate dateTo
    ) {
        List<OhlcvResponse> ohlcvResponses = ohlcvService.getOhlcvs(assetId, dateFrom, dateTo).stream()
                .map(OhlcvResponse::from)
                .toList();
        return ResponseEntity.ok(ohlcvResponses);
    }

}
