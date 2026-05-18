package com.jkproject.JkProject.stockTheme;
import com.jkproject.JkProject.theme.Theme;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StockThemeMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stockCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "themeId")
    private Theme theme;

    public StockThemeMap(String stockCode, Theme theme) {
        this.stockCode = stockCode;
        this.theme = theme;
    }
}