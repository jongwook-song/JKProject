package com.jkproject.JkProject.stock;

import com.jkproject.JkProject.stockHistory.StockHistoryConfig;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StockService {

    private StockRepository stockRepository;

    @Transactional
    public void fetchAndSaveStocks() {
        try {
            String apiUrl = "https://kind.krx.co.kr/corpgeneral/corpList.do?method=download";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Referer", "https://kind.krx.co.kr/");
            conn.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "EUC-KR"))) {
                String line;
                br.readLine();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            Document doc = Jsoup.parse(sb.toString());
            Elements rows = doc.select("tr");

            List<Stock> stockList = new ArrayList<>();

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 4) continue;
                Stock stock = new Stock();
                stock.setName(cols.get(0).text());
                stock.setCode(cols.get(2).text());
                stock.setType(cols.get(3).text());
                stock.setDetailType(cols.get(4).text());

                stockList.add(stock);
            }

            stockRepository.saveAll(stockList);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("주식 데이터를 수집하고 저장하는 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Stock> getStockInfoAll() {
        return stockRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Stock getStockByCode(String code) {
        return stockRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("해당 코드를 가진 주식 정보를 찾을 수 없습니다: " + code));
    }

    public void setStockAll(){
        List<Stock> stockList = stockRepository.findAll();
        StockHistoryConfig.STOCK_MAP = stockList.stream()
            .collect(Collectors.toMap(
                Stock::getCode,
                Stock::getName,
                (oldValue, newValue) -> newValue
            ));
    }

    @Transactional(readOnly = true)
    public Stock stockFindByid(String stockCode){
        return stockRepository.findById(stockCode).orElse(null);
    }
}
