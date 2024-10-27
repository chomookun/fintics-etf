package org.oopscraft.fintics.etf.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.etf.FinticsEtfConfiguration;
import org.oopscraft.fintics.etf.model.AssetSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsEtfConfiguration.class)
@RequiredArgsConstructor
class AssetRepositoryTest extends CoreTestSupport {

    private final AssetRepository assetRepository;

    @Test
    void findAll() {
        // given
        AssetEntity assetEntity = AssetEntity.builder()
                .assetId("US.SPY")
                .name("test name")
                .market("US")
                .build();
        entityManager.persist(assetEntity);
        entityManager.flush();
        // when
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId("SPY")
                .build();
        Pageable pageable = Pageable.unpaged();
        Page<AssetEntity> assetEntityPage = assetRepository.findAll(assetSearch, pageable);
        // then
        assertTrue(assetEntityPage.getContent().size() > 0);
    }

}