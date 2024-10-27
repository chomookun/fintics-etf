package org.oopscraft.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.etf.dao.DividendRepository;
import org.oopscraft.fintics.etf.model.Dividend;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DividendService {

    private final DividendRepository dividendRepository;

    public List<Dividend> getDividends(String assetId, LocalDate dateFrom, LocalDate dateTo) {
        return dividendRepository.findAllBy(assetId, dateFrom, dateTo).stream()
                .map(Dividend::from)
                .toList();
    }

}
