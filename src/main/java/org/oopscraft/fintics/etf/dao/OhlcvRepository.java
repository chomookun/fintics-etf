package org.oopscraft.fintics.etf.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OhlcvRepository extends JpaRepository<OhlcvEntity,OhlcvEntity.Pk>, JpaSpecificationExecutor<OhlcvEntity> {

    Optional<OhlcvEntity> findFirstByAssetIdOrderByDateDesc(String assetId);

    @Query("select a from OhlcvEntity a " +
            "where a.assetId = :assetId " +
            "and a.date between :dateFrom and :dateTo " +
            "order by a.date desc")
    List<OhlcvEntity> findAllBy(@Param("assetId")String assetId, @Param("dateFrom")LocalDate dateFrom, @Param("dateTo")LocalDate dateTo);

}
