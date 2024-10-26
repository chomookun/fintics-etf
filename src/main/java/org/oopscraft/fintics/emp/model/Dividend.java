package org.oopscraft.fintics.emp.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Dividend {

    private String assetId;

    private LocalDate date;

    private BigDecimal amount;

}
