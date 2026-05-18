package com.jkproject.JkProject.util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IndicatorUtils {
    public static int parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0;
        return Integer.parseInt(priceStr.replace("+", "").replace("-", "").trim());
    }

    public static double doubleParsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0.0;
        return Double.parseDouble(priceStr.replace("+", "").replace("-", "").trim());
    }

    public static double calculateRSI(List<Double> prices, int period) {
        List<Double> reversedPrices = new ArrayList<>(prices);
        Collections.reverse(reversedPrices);

        double au = 0.0, ad = 0.0;
        for (int i = 1; i <= period; i++) {
            double change = reversedPrices.get(i) - reversedPrices.get(i - 1);
            if (change > 0) au += change;
            else ad += Math.abs(change);
        }
        au /= period; ad /= period;

        for (int i = period + 1; i < reversedPrices.size(); i++) {
            double change = reversedPrices.get(i) - reversedPrices.get(i - 1);
            double gain = (change > 0) ? change : 0.0;
            double loss = (change < 0) ? Math.abs(change) : 0.0;

            au = (au * (period - 1) + gain) / period;
            ad = (ad * (period - 1) + loss) / period;
        }
        if (ad == 0) return 100.0;
        return 100.0 - (100.0 / (1.0 + rs(au, ad)));
    }

    private static double rs(double au, double ad) { return au / ad; }

    public static MacdResult calculateMACD(List<Double> prices, int shortP, int longP, int sigP) {
        List<Double> reversed = new ArrayList<>(prices);
        Collections.reverse(reversed);

        List<Double> emaShort = calculateEMA(reversed, shortP);
        List<Double> emaLong = calculateEMA(reversed, longP);

        List<Double> macdLine = new ArrayList<>();
        for (int i = 0; i < reversed.size(); i++) {
            if (i < longP - 1) macdLine.add(0.0);
            else macdLine.add(emaShort.get(i) - emaLong.get(i));
        }

        List<Double> validMacd = macdLine.subList(longP - 1, macdLine.size());
        List<Double> signalEma = calculateEMA(validMacd, sigP);

        MacdResult result = new MacdResult();
        result.macd = validMacd.get(validMacd.size() - 1);
        result.signal = signalEma.get(signalEma.size() - 1);
        result.oscillator = result.macd - result.signal;
        return result;
    }

    private static List<Double> calculateEMA(List<Double> data, int period) {
        List<Double> emaList = new ArrayList<>(Collections.nCopies(data.size(), 0.0));
        if (data.size() < period) return emaList;

        double sum = 0;
        for (int i = 0; i < period; i++) sum += data.get(i);
        emaList.set(period - 1, sum / period);

        double multiplier = 2.0 / (period + 1);
        for (int i = period; i < data.size(); i++) {
            double currentEma = (data.get(i) - emaList.get(i - 1)) * multiplier + emaList.get(i - 1);
            emaList.set(i, currentEma);
        }
        return emaList;
    }

    public static String analyzeTrend(List<Double> prices) {
        String returnMsg = "\n";
        int period = 120;
        int compareShift = 3;

        double currentSum = 0;
        for (int i = 0; i < period; i++) currentSum += prices.get(i);
        double current120MA = currentSum / period;

        double pastSum = 0;
        for (int i = compareShift; i < period + compareShift; i++) pastSum += prices.get(i);
        double past120MA = pastSum / period;

        double diffRate = ((current120MA - past120MA) / past120MA) * 100.0;

        if (diffRate > 0.01) {
            returnMsg += "🔴 현재 120선 추세: [ 상향 ]";
        } else if (diffRate < -0.01) {
            returnMsg += "🔵 현재 120선 추세: [ 하향 ]";
        } else {
            returnMsg += "현재 120선 추세: [ 보합 ]";
        }

        returnMsg += ("\n▶ 기울기 변화율: " + String.format("%.4f%%", diffRate));
//        returnMsg += ("\n▶ 현재 120선 값: " + String.format("%,.2f원", current120MA));
//        returnMsg += ("\n▶ 3봉전 120선 값: " + String.format("%,.2f원", past120MA));
        return returnMsg;
    }
}