package com.jkproject.JkProject.stockHistory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
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

    @Builder
    public StockHistory(String code, int curPrc, long trdeQty, double envelope, double deviationRate, double rsi, double macd, double signal, double oscillator, double diffRate, String type, double cntrStr) {
        this.code = code;
        this.curPrc = curPrc;
        this.trdeQty = trdeQty;
        this.envelope = envelope;
        this.deviationRate = deviationRate;
        this.rsi = rsi;
        this.macd = macd;
        this.msignal = msignal;
        this.oscillator = oscillator;
        this.diffRate = diffRate;
        this.type = type;
        this.cntrStr = cntrStr;
        this.date = LocalDateTime.now();
    }
}