package com.jkproject.JkProject.stockHistory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalData {
    private int curPrc;
    private long trdeQty;
    private double envelope;
    private double deviationRate;
    private double rsi;
    private double macd;
    private double msignal;
    private double oscillator;
    private double diffRate;
    private double cntrStr;

    private String infoMessage = "";
    private String indicatorMessage = "";
    private String cntrStrMessage = "";
}