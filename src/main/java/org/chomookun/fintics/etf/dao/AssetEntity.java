package org.chomookun.fintics.etf.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fintics_etf_asset",
    indexes = {
        @Index(name = "idx_fintics_etf_market", columnList = "market"),
        @Index(name = "idx_fintics_etf_asset_name", columnList = "name"),
        @Index(name = "idx_fintics_etf_asset_dividend_frequency", columnList = "dividend_frequency")
    })
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetEntity extends BaseEntity {

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "name")
    private String name;

    @Column(name = "market", length = 16)
    private String market;

    @Column(name = "exchange", length = 16)
    private String exchange;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "close", scale = 4)
    private BigDecimal close;

    @Column(name = "volume")
    private BigDecimal volume;

    @Column(name = "market_cap")
    private BigDecimal marketCap;

    @Column(name = "dividend_frequency")
    private Integer dividendFrequency;

    @Column(name = "dividend_yield", scale = 2)
    private BigDecimal dividendYield;

    @Column(name = "capital_gain", scale = 2)
    private BigDecimal capitalGain;

    @Column(name = "total_return", scale = 2)
    private BigDecimal totalReturn;

}
