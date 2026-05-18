package com.jkproject.JkProject.stockTheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockThemeResDto {
    private String themeId;
    private String stockCode;
    private String themeName;

    public StockThemeResDto(Long themeId, String stockCode, String themeName) {
        this.themeId = String.valueOf(themeId);
        this.stockCode = stockCode;
        this.themeName = themeName;
    }
}