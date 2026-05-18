package com.jkproject.JkProject.stockTheme;

import com.jkproject.JkProject.stockTheme.dto.StockThemeResDto;
import com.jkproject.JkProject.theme.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockThemeMapRepository extends JpaRepository<StockThemeMap, Long> {
    boolean existsByStockCodeAndTheme(String stockCode, Theme theme);

    @Query("SELECT new com.jkproject.JkProject.stockTheme.dto.StockThemeResDto(t.id, stm.stockCode, t.name) " +
            "FROM StockThemeMap stm " +
            "JOIN stm.theme t " +
            "WHERE stm.stockCode = :stockCode")
    List<StockThemeResDto> findThemeInfoByStockCode(@Param("stockCode") String stockCode);
//    void deleteAllInBatch();
}