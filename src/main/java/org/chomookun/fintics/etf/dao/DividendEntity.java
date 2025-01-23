package org.chomookun.fintics.etf.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fintics_etf_dividend")
@IdClass(DividendEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DividendEntity extends BaseEntity {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private LocalDate date;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Id
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "amount", scale = 4)
    private BigDecimal amount;

}
