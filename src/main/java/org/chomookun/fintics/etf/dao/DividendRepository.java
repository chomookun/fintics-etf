package org.chomookun.fintics.etf.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, DividendEntity.Pk>, JpaSpecificationExecutor<DividendEntity> {

    Optional<DividendEntity> findFirstByAssetIdOrderByDateDesc(String assetId);

    @Query("select a from DividendEntity a " +
            "where a.assetId=:assetId and a.date between :dateFrom and :dateTo " +
            "order by a.date desc")
    List<DividendEntity> findAllBy(@Param("assetId")String assetId, @Param("dateFrom")LocalDate dateFrom, @Param("dateTo")LocalDate dateTo);

}
