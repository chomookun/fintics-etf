package org.chomookun.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.dao.AssetEntity;
import org.chomookun.fintics.etf.dao.AssetRepository;
import org.chomookun.fintics.etf.model.Asset;
import org.chomookun.fintics.etf.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public Page<Asset> getAssets(AssetSearch assetSearch, Pageable pageable) {
        Page<AssetEntity> assetEntityPage = assetRepository.findAll(assetSearch, pageable);
        List<Asset> assets = assetEntityPage.getContent().stream()
                .map(Asset::from)
                .toList();
        long total = assetEntityPage.getTotalElements();
        return new PageImpl<>(assets, pageable, total);
    }

}
