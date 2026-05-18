package com.jkproject.JkProject.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StockResDto {
    private String code;
    private String name;
    private String date;
    private String type;
    private String searchType;
    private int curPrc;
    private double diffRate;
    private double rsi;
    private double macd;
    private double deviationRate;
    private double cntrStr;
}