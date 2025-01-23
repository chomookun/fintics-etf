package org.chomookun.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.service.AssetService;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.etf.FinticsEtfApplication;
import org.chomookun.fintics.etf.dao.AssetEntity;
import org.chomookun.fintics.etf.model.Asset;
import org.chomookun.fintics.etf.model.AssetSearch;
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