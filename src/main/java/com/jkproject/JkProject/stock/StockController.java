package com.jkproject.JkProject.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/sync")
    public ResponseEntity<String> syncStocks() {
        stockService.fetchAndSaveStocks();
        return ResponseEntity.ok("성공적으로 주식 데이터를 DB에 저장했습니다.");
    }

    @RequestMapping("/load")
    public List<Stock> getStockInfoAll(){
        return stockService.getStockInfoAll();
    }

    @GetMapping("/{code}")
    public Stock getStockByCode(@PathVariable("code") String code) {
        return stockService.getStockByCode(code);
    }
}
