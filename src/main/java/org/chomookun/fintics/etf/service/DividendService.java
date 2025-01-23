package org.chomookun.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.etf.dao.DividendRepository;
import org.chomookun.fintics.etf.model.Dividend;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DividendService {

    private final DividendRepository dividendRepository;

    public List<Dividend> getDividends(String assetId, LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Optional.ofNullable(dateFrom)
                .orElse(LocalDate.now().minusYears(10));
        dateTo = Optional.ofNullable(dateTo)
                .orElse(LocalDate.now());
        return dividendRepository.findAllBy(assetId, dateFrom, dateTo).stream()
                .map(Dividend::from)
                .toList();
    }

}
