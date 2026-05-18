package com.jkproject.JkProject.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkproject.JkProject.stockHistory.StockHistoryConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartUtils {
    public static List<Candle> extractAndCalculate(String jsonBody, int visibleCount, int maPeriod) throws Exception {
        JsonNode dataList = StockHistoryConfig.MAPPER.readTree(jsonBody).path("stk_min_pole_chart_qry");
        if (dataList.isMissingNode() || !dataList.isArray()) return null;

        List<Candle> allCandles = new ArrayList<>();
        for (JsonNode node : dataList) {
            Candle c = new Candle();
            c.open = IndicatorUtils.doubleParsePrice(node.path("open_pric").asText());
            c.high = IndicatorUtils.doubleParsePrice(node.path("high_pric").asText());
            c.low = IndicatorUtils.doubleParsePrice(node.path("low_pric").asText());
            c.close = IndicatorUtils.doubleParsePrice(node.path("cur_prc").asText());
            allCandles.add(c);
        }

        List<Candle> visibleCandles = new ArrayList<>();
        int limit = Math.min(visibleCount, allCandles.size());

        for (int i = 0; i < limit; i++) {
            Candle c = allCandles.get(i);
            if (i + maPeriod <= allCandles.size()) {
                double sum = 0;
                for (int j = i; j < i + maPeriod; j++) sum += allCandles.get(j).close;
                c.ma120 = sum / maPeriod;
            } else {
                c.ma120 = c.close;
            }
            visibleCandles.add(c);
        }
        Collections.reverse(visibleCandles);
        return visibleCandles;
    }

    public static void drawProChart(List<Candle> candles, File outputFile) throws Exception {
        int width = 900, height = 500, padding = 40, rightMargin = 100;
        int chartWidth = width - rightMargin;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(25, 28, 36));
        g2d.fillRect(0, 0, width, height);

        double maxPrice = Double.MIN_VALUE, minPrice = Double.MAX_VALUE;
        for (Candle c : candles) {
            maxPrice = Math.max(maxPrice, Math.max(c.high, c.ma120));
            minPrice = Math.min(minPrice, Math.min(c.low, c.ma120));
        }

        double priceRange = Math.max(maxPrice - minPrice, 1);
        maxPrice += priceRange * 0.1;
        minPrice -= priceRange * 0.1;

        double yRatio = (height - 2 * padding) / (maxPrice - minPrice);
        int xStep = (chartWidth - 2 * padding) / Math.max(1, candles.size());
        int candleWidth = Math.max(1, xStep - 4);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        for (int i = 0; i <= 5; i++) {
            double priceMark = minPrice + ((maxPrice - minPrice) * i / 5.0);
            int y = height - padding - (int) ((priceMark - minPrice) * yRatio);
            g2d.setColor(new Color(60, 60, 60));
            g2d.drawLine(padding, y, chartWidth, y);
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString(String.format("%,.0f", priceMark), chartWidth + 10, y + 5);
        }
        g2d.setColor(new Color(80, 80, 80));
        g2d.drawLine(chartWidth, padding, chartWidth, height - padding);

        for (int i = 0; i < candles.size(); i++) {
            Candle c = candles.get(i);
            int x = padding + (i * xStep) + (xStep / 2);
            int yOpen = height - padding - (int) ((c.open - minPrice) * yRatio);
            int yClose = height - padding - (int) ((c.close - minPrice) * yRatio);
            int yHigh = height - padding - (int) ((c.high - minPrice) * yRatio);
            int yLow = height - padding - (int) ((c.low - minPrice) * yRatio);

            g2d.setColor(c.close >= c.open ? new Color(255, 60, 60) : new Color(60, 150, 255));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x, yHigh, x, yLow);
            g2d.fillRect(x - (candleWidth / 2), Math.min(yOpen, yClose), candleWidth, Math.max(2, Math.abs(yOpen - yClose)));
        }

        g2d.setColor(new Color(255, 150, 0));
        g2d.setStroke(new BasicStroke(3.0f));
        int[] xPoints = new int[candles.size()], yPoints = new int[candles.size()];
        for (int i = 0; i < candles.size(); i++) {
            xPoints[i] = padding + (i * xStep) + (xStep / 2);
            yPoints[i] = height - padding - (int) ((candles.get(i).ma120 - minPrice) * yRatio);
        }
        g2d.drawPolyline(xPoints, yPoints, candles.size());

        g2d.dispose();
        ImageIO.write(image, "png", outputFile);
    }
}