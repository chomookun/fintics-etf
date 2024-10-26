package org.oopscraft.fintics.emp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, DividendEntity.Pk>, JpaSpecificationExecutor<DividendEntity> {

}
