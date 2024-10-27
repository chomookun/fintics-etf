package org.oopscraft.fintics.etf.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OhlcvRepository extends JpaRepository<OhlcvEntity,OhlcvEntity.Pk>, JpaSpecificationExecutor<OhlcvEntity> {

    List<OhlcvEntity> findAllByAssetIdOrderByDate(String assetId);

}
