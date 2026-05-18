package com.jkproject.JkProject.stockHistory.dto;

import com.jkproject.JkProject.stockTheme.dto.StockThemeResDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StockHotResDto {
    private String stockCode;
    private String stockName;
    private String type;
    private String searchType;
    private List<StockThemeResDto> stockThemes;
    private String date;
    private double diffRate;
    private long volume;
}