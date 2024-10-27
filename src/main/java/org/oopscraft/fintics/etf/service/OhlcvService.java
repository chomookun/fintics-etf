package org.oopscraft.fintics.etf.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.etf.dao.OhlcvRepository;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OhlcvService {

    private final OhlcvRepository ohlcvRepository;

    public List<Ohlcv> getOhlcvs(String assetId, LocalDate dateFrom, LocalDate dateTo) {
        return ohlcvRepository.findAllBy(assetId, dateFrom, dateTo).stream()
                .map(Ohlcv::from)
                .toList();
    }

}
