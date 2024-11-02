package org.oopscraft.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.etf.dao.OhlcvRepository;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OhlcvService {

    private final OhlcvRepository ohlcvRepository;

    public List<Ohlcv> getOhlcvs(String assetId, LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Optional.ofNullable(dateFrom)
                .orElse(LocalDate.now().minusYears(10));
        dateTo = Optional.ofNullable(dateTo)
                .orElse(LocalDate.now());
        return ohlcvRepository.findAllBy(assetId, dateFrom, dateTo).stream()
                .map(Ohlcv::from)
                .toList();
    }

}
