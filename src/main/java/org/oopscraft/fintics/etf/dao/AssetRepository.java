package org.oopscraft.fintics.etf.dao;

import org.oopscraft.fintics.etf.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity,String>, JpaSpecificationExecutor<AssetEntity> {

    default Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable) {
        // where condition
        Specification<AssetEntity> specification = Specification.where(null);
        if (assetSearch.getAssetId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(AssetEntity_.ASSET_ID), '%' + assetSearch.getAssetId() + '%'));
        }
        if (assetSearch.getName() != null) {
            specification = specification.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(AssetEntity_.NAME), '%' + assetSearch.getName() + '%')));
        }
        // returns
        return findAll(specification, pageable);
    }

}
