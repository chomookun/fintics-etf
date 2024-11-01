package org.oopscraft.fintics.etf.collector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.oopscraft.fintics.etf.dao.*;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractMarketCollector {

    private final PlatformTransactionManager transactionManager;

    private final AssetRepository assetRepository;

    private final DividendRepository dividendRepository;

    private final OhlcvRepository ohlcvRepository;

    @Getter
    private RestTemplate restTemplate;

    public AbstractMarketCollector(PlatformTransactionManager transactionManager, AssetRepository assetRepository, DividendRepository dividendRepository, OhlcvRepository ohlcvRepository) {
        this.transactionManager = transactionManager;
        this.assetRepository = assetRepository;
        this.dividendRepository = dividendRepository;
        this.ohlcvRepository = ohlcvRepository;

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .retryCount(3)
                .build();
    }

    void collect() {
        List<Asset> assets = getAssets();
        for (Asset asset : assets) {
            try {
                // asset
                saveAsset(asset, transactionManager, assetRepository);

                // dividends
                LocalDate dividendDateFrom = dividendRepository.findFirstByAssetIdOrderByDateDesc(asset.getAssetId())
                        .map(DividendEntity::getDate)
                        .orElse(LocalDate.now().minusYears(30))
                        .plusDays(1);
                LocalDate dividendDateTo = LocalDate.now();
                List<Dividend> dividends = getDividends(asset, dividendDateFrom, dividendDateTo);
                saveDividends(dividends, transactionManager, dividendRepository);

                // ohlcvs
                LocalDate ohlcvDateFrom = ohlcvRepository.findFirstByAssetIdOrderByDateDesc(asset.getAssetId())
                        .map(OhlcvEntity::getDate)
                        .orElse(LocalDate.now().minusYears(30))
                        .plusDays(1);
                LocalDate ohlcvDateTo = LocalDate.now();
                List<Ohlcv> ohlcvs = getOhlcvs(asset, ohlcvDateFrom, ohlcvDateTo);
                saveOhlcvs(ohlcvs, transactionManager, ohlcvRepository);

            } catch (Throwable t) {
                log.warn(t.getMessage());
            } finally {
                try {
                    Thread.sleep(10_000);
                } catch (Throwable ignore) {}
            }
        }
    }

    abstract List<Asset> getAssets();

    abstract List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo);

    abstract List<Ohlcv> getOhlcvs(Asset asset, LocalDate dateFrom, LocalDate dateTo);

    void saveAsset(Asset asset, PlatformTransactionManager transactionManager, AssetRepository assetRepository) {
        AssetEntity assetEntity = AssetEntity.builder()
                .assetId(asset.getAssetId())
                .name(asset.getName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .marketCap(asset.getMarketCap())
                .dividendYield(asset.getDividendYield())
                .dividendFrequency(asset.getDividendFrequency())
                .build();
        saveEntities("asset", List.of(assetEntity), transactionManager, assetRepository);
    }

    void saveDividends(List<Dividend> dividends, PlatformTransactionManager transactionManager, DividendRepository dividendRepository) {
        List<DividendEntity> dividendEntities = dividends.stream()
                .map(it -> DividendEntity.builder()
                        .assetId(it.getAssetId())
                        .date(it.getDate())
                        .amount(it.getAmount())
                        .build())
                .collect(Collectors.toList());
        saveEntities("dividends", dividendEntities, transactionManager, dividendRepository);
    }

    void saveOhlcvs(List<Ohlcv> ohlcvs, PlatformTransactionManager transactionManager, OhlcvRepository ohlcvRepository) {
        List<OhlcvEntity> ohlcvEntities = ohlcvs.stream()
                .map(it -> OhlcvEntity.builder()
                        .assetId(it.getAssetId())
                        .date(it.getDate())
                        .open(it.getOpen())
                        .high(it.getHigh())
                        .low(it.getLow())
                        .close(it.getClose())
                        .volume(it.getVolume())
                        .build())
                .collect(Collectors.toList());
        saveEntities("ohlcvs", ohlcvEntities, transactionManager, ohlcvRepository);
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

    /**
     * convert to string to number
     * @param value number string
     * @param defaultValue default number
     * @return converted number
     */
    BigDecimal toNumber(Object value, BigDecimal defaultValue) {
        try {
            String valueString = value.toString().replace(",", "");
            return new BigDecimal(valueString);
        }catch(Throwable e){
            return defaultValue;
        }
    }

    String toAssetId(String market, String symbol) {
        return String.format("%s.%s", market, symbol);
    }

}
