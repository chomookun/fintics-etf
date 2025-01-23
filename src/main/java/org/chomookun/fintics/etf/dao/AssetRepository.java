package org.chomookun.fintics.etf.dao;

import org.chomookun.fintics.etf.model.AssetSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        if (assetSearch.getMarket() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(AssetEntity_.MARKET), assetSearch.getMarket()));
        }
        if (assetSearch.getDividendFrequencyMin() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(AssetEntity_.DIVIDEND_FREQUENCY), assetSearch.getDividendFrequencyMin()));
        }
        if (assetSearch.getDividendFrequencyMax() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(AssetEntity_.DIVIDEND_FREQUENCY), assetSearch.getDividendFrequencyMax()));
        }

        // default sort
        Sort sort = pageable.getSortOr(Sort.by(AssetEntity_.MARKET_CAP).descending());

        // find
        if (pageable.isPaged()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return findAll(specification, pageable);
        } else {
            List<AssetEntity> assetEntities = findAll(specification, sort);
            return new PageImpl<>(assetEntities, pageable, assetEntities.size());
        }
    }

}
