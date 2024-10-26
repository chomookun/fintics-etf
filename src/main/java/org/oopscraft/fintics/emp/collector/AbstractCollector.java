package org.oopscraft.fintics.emp.collector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class AbstractCollector {

    @Getter
    private RestTemplate restTemplate;

    public AbstractCollector() {
        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .retryCount(3)
                .build();
    }

    /**
     * chunk save entities via specified repository
     * @param unitName unit name
     * @param entities entities
     * @param transactionManager transaction manager
     * @param jpaRepository jpa repository
     * @param <T> entity type
     * @param <P> id class type
     */
    protected <T, P> void saveEntities(String unitName, List<T> entities, PlatformTransactionManager transactionManager, JpaRepository<T,P> jpaRepository) {
        if (entities.isEmpty()) {
            return;
        }
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            int count = 0;
            for (T ohlcvEntity : entities) {
                count++;
                jpaRepository.saveAndFlush(ohlcvEntity);
                // middle commit
                if (count % 10 == 0) {
                    log.debug("- {} chunk commit[{}]", unitName, count);
                    transactionManager.commit(status);
                    status = transactionManager.getTransaction(definition);
                }
            }
            // final commit
            log.debug("- {} final commit[{}]", unitName, count);
            transactionManager.commit(status);
            log.info("- {} saved[{}]", unitName, count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(status);
        } finally {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }

}
