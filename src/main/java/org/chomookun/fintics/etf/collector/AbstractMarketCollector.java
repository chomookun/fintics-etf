package org.chomookun.fintics.etf.collector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.chomookun.fintics.etf.dao.*;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.etf.model.Asset;
import org.chomookun.fintics.etf.model.Dividend;
import org.chomookun.fintics.etf.model.Ohlcv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();
    }

    void collect() {
        List<Asset> assets = getAssets();
        for (Asset asset : assets) {
            try {
                // check updated today
                AssetEntity assetEntity = assetRepository.findById(asset.getAssetId()).orElse(null);
                if (assetEntity != null) {
                    if (assetEntity.getUpdatedDate() != null && assetEntity.getUpdatedDate().isEqual(LocalDate.now())) {
                        continue;
                    }
                }

                // saves asset
                Map<String,String> assetDetail = getAssetDetail(asset);
                asset.setUpdatedDate(LocalDate.now());
                asset.setClose(Optional.ofNullable(assetDetail.get("close"))
                        .map(BigDecimal::new)
                        .orElse(null));
                asset.setVolume(Optional.ofNullable(assetDetail.get("volume"))
                        .map(BigDecimal::new)
                        .orElse(null));
                asset.setMarketCap(Optional.ofNullable(assetDetail.get("marketCap"))
                        .map(BigDecimal::new)
                        .orElse(null));
                asset.setDividendYield(Optional.ofNullable(assetDetail.get("dividendYield"))
                        .map(BigDecimal::new)
                        .orElse(null));
                asset.setDividendFrequency(Optional.ofNullable(assetDetail.get("dividendFrequency"))
                        .map(Integer::parseInt)
                        .orElse(null));
                asset.setCapitalGain(Optional.ofNullable(assetDetail.get("capitalGain"))
                        .map(BigDecimal::new)
                        .orElse(null));
                asset.setTotalReturn(Optional.ofNullable(assetDetail.get("totalReturn"))
                        .map(BigDecimal::new)
                        .orElse(null));
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
            }
        }
    }

    abstract List<Asset> getAssets();

    abstract Map<String,String> getAssetDetail(Asset asset);

    abstract List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo);

    abstract List<Ohlcv> getOhlcvs(Asset asset, LocalDate dateFrom, LocalDate dateTo);

    static void sleep() {
        try {
            Thread.sleep(1_000);
        } catch (Throwable ignore) {}
    }

    void saveAsset(Asset asset, PlatformTransactionManager transactionManager, AssetRepository assetRepository) {
        AssetEntity assetEntity = AssetEntity.builder()
                .assetId(asset.getAssetId())
                .name(asset.getName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .updatedDate(asset.getUpdatedDate())
                .close(asset.getClose())
                .volume(asset.getVolume())
                .marketCap(asset.getMarketCap())
                .dividendYield(asset.getDividendYield())
                .dividendFrequency(asset.getDividendFrequency())
                .capitalGain(asset.getCapitalGain())
                .totalReturn(asset.getTotalReturn())
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

    String toAssetId(String market, String symbol) {
        return String.format("%s.%s", market, symbol);
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

    /**
     * converts string to number
     * @param value string
     * @return number
     */
    BigDecimal convertStringToNumber(String value) {
        if (value == null) {
            return null;
        }
        value = value.replace(",", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return null;
        }
    }

    /**
     * converts currency string to number
     * @param value currency string
     * @return currency number
     */
    BigDecimal convertCurrencyToNumber(String value, Currency currency) {
        if (value == null) {
            return null;
        }
        try {
            value = value.replace(currency.getSymbol(), "");
            value = value.replace(",","");
            return new BigDecimal(value);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * converts percentage string to number
     * @param value percentage string
     * @return percentage number
     */
    BigDecimal convertPercentageToNumber(String value) {
        value = value.replace("%", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return null;
        }
    }

}
