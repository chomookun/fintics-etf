package org.oopscraft.fintics.etf.api.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.web.common.data.PageableUtils;
import org.oopscraft.fintics.etf.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.AssetSearch;
import org.oopscraft.fintics.etf.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@Tag(name = "assets", description = "Assets")
@RequiredArgsConstructor
@Slf4j
public class AssetRestController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "asset_id", required = false) String assetId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "market", required = false) String market,
            @RequestParam(value = "dividendYieldMin", required = false) BigDecimal dividendYieldMin,
            @RequestParam(value = "dividendYieldMax", required = false) BigDecimal dividendYieldMax,
            @RequestParam(value = "dividendFrequencyMin", required = false) Integer dividendFrequencyMin,
            @RequestParam(value = "dividendFrequencyMax", required = false) Integer dividendFrequencyMax,
            @PageableDefault Pageable pageable
    ) {
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .name(name)
                .market(market)
                .dividendYieldMin(dividendYieldMin)
                .dividendYieldMax(dividendYieldMax)
                .dividendFrequencyMin(dividendFrequencyMin)
                .dividendFrequencyMax(dividendFrequencyMax)
                .build();
        Page<Asset> assetPage = assetService.getAssets(assetSearch, pageable);
        List<AssetResponse> assetResponses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .toList();
        long count = assetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, count))
                .body(assetResponses);
    }

}
