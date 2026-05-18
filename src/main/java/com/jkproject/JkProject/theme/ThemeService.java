package com.jkproject.JkProject.theme;

import com.jkproject.JkProject.stock.StockRepository;
import com.jkproject.JkProject.stockHistory.StockHistoryConfig;
import com.jkproject.JkProject.stockTheme.StockThemeMap;
import com.jkproject.JkProject.stockTheme.StockThemeMapRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final StockThemeMapRepository mapRepository;
    private final StockRepository stockRepository;
    private final String NAVER_FINANCE_URL = "https://finance.naver.com";

    @Transactional
    public void syncThemesFromNaver() {
        System.out.println("🔄 네이버 금융 테마 크롤링 및 DB 저장 시작...");

        try {
            for (int page = 1; page <= 10; page++) {
                String listUrl = NAVER_FINANCE_URL + "/sise/theme.naver?page=" + page;
                Document listDoc = Jsoup.connect(listUrl).get();
                Elements themeLinks = listDoc.select(".col_type1 a");

                if (themeLinks.isEmpty()) {
                    System.out.println("✅ " + (page - 1) + "페이지까지 모든 테마 크롤링 완료!");
                    break;
                }

                for (Element link : themeLinks) {
                    String themeName = link.text();
                    String detailHref = link.attr("href");

                    Theme theme = themeRepository.findByName(themeName)
                        .orElseGet(() -> themeRepository.save(new Theme(themeName)));

                    String detailUrl = NAVER_FINANCE_URL + detailHref;
                    Document detailDoc = Jsoup.connect(detailUrl).get();

                    Elements stockLinks = detailDoc.select(".type_5 tbody tr .name a");

                    for (Element stockLink : stockLinks) {
                        String stockHref = stockLink.attr("href");
                        String stockCode = stockHref.split("code=")[1];

                        if (!stockRepository.existsById(stockCode)) {
                             System.out.println("⚠️ DB에 없는 종목 패스: " + stockCode);
                            continue;
                        }

                        if (!mapRepository.existsByStockCodeAndTheme(stockCode, theme)) {
                            mapRepository.save(new StockThemeMap(stockCode, theme));
                        }
                    }

                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 테마 크롤링 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setStockAll(){
        List<Theme> themeList = themeRepository.findAll();
        StockHistoryConfig.THEME_MAP = themeList.stream()
            .collect(Collectors.toMap(
                Theme::getId,
                Theme::getName,
                (oldValue, newValue) -> newValue
            ));
    }
}