package org.chomookun.fintics.etf.api.v1;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.api.v1.dto.DividendResponse;
import org.chomookun.fintics.etf.service.DividendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dividends")
@RequiredArgsConstructor
public class DividendsRestController {

    private final DividendService dividendService;

    @GetMapping("{assetId}")
    public ResponseEntity<List<DividendResponse>> getDividends(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo
    ) {
        List<DividendResponse> dividendResponses = dividendService.getDividends(assetId, dateFrom, dateTo).stream()
                .map(DividendResponse::from)
                .toList();
        return ResponseEntity.ok(dividendResponses);
    }

}
