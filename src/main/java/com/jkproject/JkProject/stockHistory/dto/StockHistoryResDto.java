package com.jkproject.JkProject.stockHistory.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockHistoryResDto {
    private String name;
    private int curPrc;
    private long trdeQty;
    private double envelope;
    private double deviationRate;
    private double rsi;
    private double macd;
    private double msignal;
    private double oscillator;
    private double diffRate;
    private String type;
    private double cntrStr;
    private LocalDateTime date;
}