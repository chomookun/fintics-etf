package org.oopscraft.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.etf.FinticsEtfApplication;
import org.oopscraft.fintics.etf.dao.AssetEntity;
import org.oopscraft.fintics.etf.dao.AssetRepository;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.AssetSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsEtfApplication.class)
@RequiredArgsConstructor
class AssetServiceTest extends CoreTestSupport {

    private final AssetService assetService;

    @Test
    void getAssets() {
        // given
        String assetId = "test";
        entityManager.persist(AssetEntity.builder()
                .assetId(assetId)
                .build());
        entityManager.flush();
        // when
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .build();
        Page<Asset> assetPage = assetService.getAssets(assetSearch, Pageable.unpaged());
        // then
        assertTrue(assetPage.getContent().size() > 0);
    }

}