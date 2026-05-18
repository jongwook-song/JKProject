package com.jkproject.JkProject.stockTheme;

import com.jkproject.JkProject.stockTheme.dto.StockThemeResDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class StockThemeMapService {

    private StockThemeMapRepository stockThemeMapRepository;

    public List<StockThemeResDto> findAllByStockCodeIn(String stockCode) {
        return stockThemeMapRepository.findThemeInfoByStockCode(stockCode);
    }
}
