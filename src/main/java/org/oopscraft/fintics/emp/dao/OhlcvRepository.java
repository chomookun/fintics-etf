package org.oopscraft.fintics.emp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OhlcvRepository extends JpaRepository<OhlcvEntity,OhlcvEntity.Pk>, JpaSpecificationExecutor<OhlcvEntity> {

}
