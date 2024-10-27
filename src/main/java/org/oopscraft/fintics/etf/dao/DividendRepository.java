package org.oopscraft.fintics.etf.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, DividendEntity.Pk>, JpaSpecificationExecutor<DividendEntity> {

    List<DividendEntity> findAllByAssetIdOrderByDate(String assetId);

}
