package com.jkproject.JkProject.stockHistory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.jkproject.JkProject.stock.Stock;
import com.jkproject.JkProject.stock.StockService;
import com.jkproject.JkProject.stock.dto.StockResDto;
import com.jkproject.JkProject.stockHistory.dto.StockHistoryResDto;
import com.jkproject.JkProject.stockHistory.dto.StockHotResDto;
import com.jkproject.JkProject.stockTheme.StockThemeMapService;
import com.jkproject.JkProject.stockTheme.dto.StockThemeResDto;
import com.jkproject.JkProject.util.IndicatorUtils;
import com.jkproject.JkProject.util.MacdResult;
import com.jkproject.JkProject.util.TelegramService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StockHistoryService {
    private static String appKey;
    private static String secretKey;

    @Value("${kiwoom.app.key}")
    public void setAppKey(String value) {
        appKey = value;
    }

    @Value("${kiwoom.secret.key}")
    public void setSecretKey(String value) {
        secretKey = value;
    }

    private StockHistoryRepository stockHistoryRepository;
    private StockService stockService;
    private TelegramService telegramService;
    private final StockThemeMapService stockThemeMapService;
    private SimpMessagingTemplate messagingTemplate; // ✨ 추가됨
    //"13","개선3" [["14","ER3이격도"],["15","거래대금 집중"],["16","지수대비강세"],["17","ER3개선"],["7","ER3"],["9","기관 외국인 수급"]]

    public static final HashMap<String, String> conditionMap = new HashMap<>() {{
        put("7", "ER3");
        put("17", "ER3개선");
        put("13", "개선");
        put("14", "ER3이격도");
        put("15", "거래대금 급증");
        put("16", "지수대비 강세");
    }};

    private static String lastSectorName = "";
    private static String lastThemeName = "";

    public String getAccessToken() throws Exception {
        String accessToken = "";

        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("grant_type", "client_credentials");
        jsonData.addProperty("appkey", appKey);
        jsonData.addProperty("secretkey", secretKey);

        String urlString = StockHistoryConfig.REST_URL + "/oauth2/token";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonData.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (Scanner scanner = new Scanner(connection.getInputStream(), "utf-8")) {
                String responseBody = scanner.useDelimiter("\\A").next();
                JsonNode rootNode = StockHistoryConfig.MAPPER.readTree(responseBody);

                accessToken = rootNode.path("token").asText();
            }
        } else {
            System.err.println("❌ 토큰 발급 실패 (HTTP " + responseCode + ")");
            try (Scanner scanner = new Scanner(connection.getErrorStream(), "utf-8")) {
                System.err.println(scanner.useDelimiter("\\A").next());
            }
        }

        return accessToken;
    }

    public static HttpResponse<String> get3ChartData(String stockCode){
        try {
            Map<String, String> chartMap = new LinkedHashMap<>();
            chartMap.put("stk_cd", stockCode);
            chartMap.put("upd_stkpc_tp", "1");
            chartMap.put("tic_scope", "3");
            chartMap.put("adj_prc", "1");

            HttpRequest chartReq = HttpRequest.newBuilder()
                .uri(URI.create(StockHistoryConfig.REST_URL + "/api/dostk/chart"))
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("authorization", "Bearer " + StockHistoryConfig.accessToken)
                .header("appkey", appKey)
                .header("secretkey", secretKey)
                .header("api-id", "ka10080")
                .POST(HttpRequest.BodyPublishers.ofString(StockHistoryConfig.MAPPER.writeValueAsString(chartMap)))
                .build();


            return StockHistoryConfig.HTTP_CLIENT.send(chartReq, HttpResponse.BodyHandlers.ofString());
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public void analyzeHotStock(String stockCode, Stock stockMaster, String searchType, String formatTime) throws JsonProcessingException {
        double diffRate = 0.0;
        long todayTotalVolume = 0;

        HttpResponse<String> chartRes = get3ChartData(stockCode);

        if (chartRes != null && chartRes.statusCode() == 200) {
            String body = chartRes.body();
            JsonNode chartDataList = StockHistoryConfig.MAPPER.readTree(body).path("stk_min_pole_chart_qry");

//                                if (chartDataList.isMissingNode() || !chartDataList.isArray() || chartDataList.size() < 123) {
//                                    data.setIndicatorMessage("❌ 데이터를 가져오지 못했거나 과거 데이터 부족");
//                                    return data;
//                                }
            double currentPrice = Math.abs(chartDataList.get(0).path("cur_prc").asDouble());
            double yesterdayClosePrice = 0.0;
            String todayDate = chartDataList.get(0).path("cntr_tm").asText().substring(0, 8);
            for (JsonNode node : chartDataList) {
                String date = node.path("cntr_tm").asText().substring(0, 8);
                long volume = Math.abs(node.path("trde_qty").asLong());
                double price = Math.abs(node.path("cur_prc").asDouble());

                if (date.equals(todayDate)) {
                    // 오늘 날짜인 동안에는 거래량을 계속 더함
                    todayTotalVolume += volume;
                } else {
                    // 날짜가 어제로 바뀌는 첫 번째 캔들 = 어제 장 마감 마지막 봉 (전일 종가)
                    yesterdayClosePrice = price;
                    break;
                }
            }

            if (yesterdayClosePrice > 0) {
                // 당일 등락률 = (현재가 - 전일종가) / 전일종가 * 100
                diffRate = (currentPrice - yesterdayClosePrice) / yesterdayClosePrice * 100.0;
            } else {
                // 신규 상장주라 어제 데이터가 없는 경우, 오늘 첫 봉의 '시가'를 기준으로 잡는 예외 처리
                JsonNode oldestTodayCandle = chartDataList.get(chartDataList.size() - 1);
                double todayOpenPrice = Math.abs(oldestTodayCandle.path("시가").asDouble());
                if(todayOpenPrice > 0) {
                    diffRate = (currentPrice - todayOpenPrice) / todayOpenPrice * 100.0;
                }
            }
        }

        List<StockThemeResDto> themeDto = stockThemeMapService.findAllByStockCodeIn(stockCode);
        StockHotResDto hotStock = StockHotResDto.builder()
            .stockCode(stockCode)
            .stockName(stockMaster.getName())
            .type(stockMaster.getType())
            .searchType(searchType)
            .date(formatTime)
            .diffRate(diffRate)
            .stockThemes(themeDto)
            .volume(todayTotalVolume)
            .build();

        StockHistoryConfig.detectedStocksMap.put(stockCode, hotStock);

        if (StockHistoryConfig.detectedStocksMap.size() >= 2) {
            String alertMsg = analyzeMarketLeaders(new ArrayList<>(StockHistoryConfig.detectedStocksMap.values()));

            // 분석 결과가 잘 나왔다면 프론트엔드 알림 채널로 쏴줍니다.
//                                if (!alertMsg.isEmpty()) {
//                                    messagingTemplate.convertAndSend("/topic/alerts", alertMsg);
//                                }
        }
    }
    public static SignalData analyzeStockData(String stockCode) {
        SignalData data = new SignalData();

        try {
            Map<String, String> infoMap = new LinkedHashMap<>();
            infoMap.put("stk_cd", stockCode);

            HttpRequest infoReq = HttpRequest.newBuilder()
                .uri(URI.create(StockHistoryConfig.REST_URL + "/api/dostk/stkinfo"))
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("authorization", "Bearer " + StockHistoryConfig.accessToken)
                .header("appkey", appKey)
                .header("secretkey", secretKey)
                .header("api-id", "ka10001")
                .POST(HttpRequest.BodyPublishers.ofString(StockHistoryConfig.MAPPER.writeValueAsString(infoMap)))
                .build();

            HttpResponse<String> infoRes = StockHistoryConfig.HTTP_CLIENT.send(infoReq, HttpResponse.BodyHandlers.ofString());

            if (infoRes.statusCode() == 200) {
                JsonNode json = StockHistoryConfig.MAPPER.readTree(infoRes.body());

                data.setTrdeQty(IndicatorUtils.parsePrice(json.path("trde_qty").asText()));

                // 문자열 조립
//                data.infoMessage = "시가: " + String.format("%,d원", IndicatorUtils.parsePrice(json.path("open_pric").asText())) +
//                        "\n저가: " + String.format("%,d원", IndicatorUtils.parsePrice(json.path("low_pric").asText())) +
//                        "\n고가: " + String.format("%,d원", IndicatorUtils.parsePrice(json.path("high_pric").asText())) +
//                        "\n거래량: <b>" + String.format("%,d", data.trdeQty) + "</b>";
                data.setInfoMessage("거래량: " + String.format("%,d", data.getTrdeQty()));
            } else {
                data.setInfoMessage("종목 정보 API 호출 에러: " + infoRes.body());
            }

            // ==========================================================
            // 2. 3분봉 차트 데이터 호출 (ka10080) - 지표 계산 및 차트 그리기
            // ==========================================================
            HttpResponse<String> chartRes = get3ChartData(stockCode);

            if (chartRes != null && chartRes.statusCode() == 200) {
                String body = chartRes.body();
                JsonNode chartDataList = StockHistoryConfig.MAPPER.readTree(body).path("stk_min_pole_chart_qry");

                if (chartDataList.isMissingNode() || !chartDataList.isArray() || chartDataList.size() < 123) {
                    data.setIndicatorMessage("❌ 데이터를 가져오지 못했거나 과거 데이터 부족");
                    return data;
                }

                // 종가 리스트 추출
                List<Double> prices = new ArrayList<>();
                List<Double> openPrices = new ArrayList<>();

                for (JsonNode node : chartDataList) {
                    prices.add(IndicatorUtils.doubleParsePrice(node.path("cur_prc").asText()));
                    openPrices.add(IndicatorUtils.doubleParsePrice(node.path("open_pric").asText()));
                }

                data.setCurPrc(prices.get(0).intValue());

                // Envelope 계산
                double sum25 = 0;
                for (int i = 0; i < 25; i++) sum25 += prices.get(i);
                data.setEnvelope(sum25 / 25.0);
                data.setDeviationRate(((data.getCurPrc() - data.getEnvelope()) / data.getEnvelope()) * 100.0);

                // 120선 기울기 (diffRate) 직접 계산
                double currentSum120 = 0, pastSum120 = 0;
                for (int i = 0; i < 120; i++) currentSum120 += prices.get(i);
                for (int i = 3; i < 123; i++) pastSum120 += prices.get(i);
                double past120MA = pastSum120 / 120.0;
                data.setDiffRate((((currentSum120 / 120.0) - past120MA) / past120MA) * 100.0);

                // RSI & MACD 계산
                data.setRsi(IndicatorUtils.calculateRSI(prices, 14));

                MacdResult macdResult = IndicatorUtils.calculateMACD(prices, 12, 26, 9);
                data.setMacd(macdResult.macd);
                data.setMsignal(macdResult.signal);
                data.setOscillator(macdResult.oscillator);

                boolean isSignalBold = data.getMacd() < data.getMsignal();
                // 텔레그램 메시지 조립
                data.setIndicatorMessage((data.getCurPrc() >= openPrices.get(0).intValue() ? "🔴" : "🔵") + "현재가: " + String.format("%,d원", data.getCurPrc()) + " ("+((data.getCurPrc() - openPrices.get(0).intValue()) / openPrices.get(0).intValue() * 100) + "%)" +
//                    "\nEnvelope(25MA): " + String.format("%,.0f원", data.envelope) +
                    "\n괴리율: " + String.format("%.2f%%", data.getDeviationRate()) +
                    IndicatorUtils.analyzeTrend(prices) +
                    "\n------------------------------\n " + (data.getRsi() < 20 ? "🔴" : "") + "현재 3분봉 RSI(14): " + String.format("%.2f", data.getRsi()) +
//                    (isSignalBold ? "<b>" : "") +
                    "\n------------------------------\n" + (data.getMacd() >= 0 ? "🔴" : "🔵") + "MACD: " + String.format("%.2f", data.getMacd()) +
                    "\n" + (data.getMsignal() >= 0 ? "🔴" : "🔵") + "Signal: " + String.format("%.2f", data.getMsignal()) +
//                    (isSignalBold ? "</b>" : "") +
                    "\n" + (data.getOscillator() >= 0 ? "🔴" : "🔵") + "오실레이터: " + String.format("%.2f", data.getOscillator()) +
                    "\n👉 상태: " + (data.getOscillator() > 0 ? "[상승 추세]" : "[하락 추세]")
                );

                // 2-4. 차트 생성
//                List<Candle> candles = ChartUtils.extractAndCalculate(body, 50, 120);
//                if (candles != null && !candles.isEmpty()) {
//                    ChartUtils.drawProChart(candles, new File(stockCode + ".png"));
//                }
            }
            else{
                if(chartRes != null){
                    data.setInfoMessage("보조 지표 API 호출 에러: " + chartRes.body());
                }
                else{
                    data.setInfoMessage("보조 지표 API 호출 에러");
                }
            }

            Map<String, String> cntRbodyMap = new LinkedHashMap<>();
            cntRbodyMap.put("stk_cd", stockCode);

            HttpRequest cntRrequest = HttpRequest.newBuilder()
                .uri(URI.create(StockHistoryConfig.REST_URL + "/api/dostk/stkinfo"))
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("authorization", "Bearer " + StockHistoryConfig.accessToken)
                .header("appkey", appKey)
                .header("secretkey", secretKey)
                .header("api-id", "ka10095")
                .header("cont-yn", "N")
                .POST(HttpRequest.BodyPublishers.ofString(StockHistoryConfig.MAPPER.writeValueAsString(cntRbodyMap)))
                .build();

            HttpResponse<String> cntRresponse = StockHistoryConfig.HTTP_CLIENT.send(cntRrequest, HttpResponse.BodyHandlers.ofString());

            if (cntRresponse.statusCode() == 200) {
                JsonNode dataArray = StockHistoryConfig.MAPPER.readTree(cntRresponse.body()).path("atn_stk_infr");

                if (dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode json = dataArray.get(0);
                    // DB용 원시 데이터 추출
                    data.setCntrStr(json.path("cntr_str").asDouble());

                    if(data.getCntrStr() > 100.5) {
                        data.setCntrStrMessage("🔴체결강도 : " +  data.getCntrStr());
                    } else if(data.getCntrStr() < 99.5) {
                        data.setCntrStrMessage("🔵체결강도 : " + data.getCntrStr());
                    }
                }
            } else {
                data.setInfoMessage("체결 정보 API 호출 에러: " + cntRresponse.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data; // 수집된 봇 데이터 반환!
    }

    @Async
    @Transactional
    public void processAsync(String stockCode, String stockName, String typeInfo, String date, String type) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String formattedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println(formattedNow + "  🚀 [" + type + " | " + stockName + "] 백그라운드 분석 및 DB 저장 시작...");

            SignalData data = analyzeStockData(stockCode);

            StockHistory entity = StockHistory.builder()
                    .code(stockCode)
                    .curPrc(data.getCurPrc())
                    .trdeQty(data.getTrdeQty())
                    .envelope(data.getEnvelope())
                    .deviationRate(data.getDeviationRate())
                    .rsi(data.getRsi())
                    .macd(data.getMacd())
                    .signal(data.getMsignal())
                    .oscillator(data.getOscillator())
                    .diffRate(data.getDiffRate())
                    .type(type)
                    .cntrStr(data.getCntrStr())
                    .build();

            stockHistoryRepository.save(entity);

            StockResDto frontendDto = new StockResDto(stockCode, stockName, formattedNow, typeInfo, type, data.getCurPrc(), data.getDiffRate(), data.getRsi(), data.getMacd(), data.getDeviationRate(), data.getCntrStr());

            messagingTemplate.convertAndSend("/topic/scanner", frontendDto);

            StringBuilder msg = new StringBuilder();
            msg.append("[ " + type + " | " + conditionMap.get(type)  + " ]").append(stockName).append(" (").append(stockCode).append(") ").append(date).append("\n");
            msg.append(typeInfo).append("\n------------------------------\n");
            msg.append(data.getInfoMessage()).append("\n------------------------------\n");
            msg.append(data.getCntrStrMessage()).append("\n------------------------------\n");
            msg.append(data.getIndicatorMessage()).append("\n------------------------------\n");

//            telegramService.sendText("chatK", msg.toString());
//            telegramService.sendText("chatS", msg.toString());

        } catch (Exception e) {
            System.err.println("❌ [" + stockCode + "] 처리 에러: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 차트 이미지 파일 삭제
            File file = new File("./" + stockCode + ".png");
//            if (file.exists()) file.delete();
        }
    }

    public List<StockHistoryResDto> getStockHistory(int page) {
        if (StockHistoryConfig.STOCK_MAP.isEmpty()) {
            System.out.println("Map이 비어 있습니다.");
            stockService.setStockAll();
        }

        Pageable pageable = PageRequest.of(page, 50, Sort.by("date").descending());
        Page<StockHistory> pageResult = stockHistoryRepository.findAll(pageable);

        List<StockHistoryResDto> responseList = pageResult.getContent().stream()
            .map(history -> {
                return StockHistoryResDto.builder()
                    .name(StockHistoryConfig.STOCK_MAP.getOrDefault(history.getCode(), "알수없음"))
                    .curPrc(history.getCurPrc())
                    .trdeQty(history.getTrdeQty())
                    .envelope(history.getEnvelope())
                    .deviationRate(history.getDeviationRate())
                    .rsi(history.getRsi())
                    .macd(history.getMacd())
                    .msignal(history.getMsignal())
                    .oscillator(history.getOscillator())
                    .diffRate(history.getDiffRate())
                    .type(history.getType())
                    .cntrStr(history.getCntrStr())
                    .date(history.getDate())
                    .build();
            })
            .collect(Collectors.toList());

        return responseList;
    }

    public String analyzeMarketLeaders(List<StockHotResDto> detectedStocks) {
        if (detectedStocks == null || detectedStocks.size() < 2) {
            return "";
        }

        // 주도 업종 분석
        Map<String, List<StockHotResDto>> sectorMap = detectedStocks.stream()
                .filter(s -> s.getType() != null && !s.getType().equals("분류안됨") && !s.getType().isEmpty())
                .collect(Collectors.groupingBy(StockHotResDto::getType));

        // 공동 1등이 있어도, 거래량 우선
        Map.Entry<String, List<StockHotResDto>> topSector = findTopSingleGroup(sectorMap);
        String currentSectorName = (topSector != null) ? topSector.getKey() : "";

        // 주도 테마 분석
        Map<String, List<StockHotResDto>> themeGroupMap = new HashMap<>();
        for (StockHotResDto stock : detectedStocks) {
            if (stock.getStockThemes() != null) {
                for (StockThemeResDto themeDto : stock.getStockThemes()) {
                    String compositeKey = themeDto.getThemeId() + "||" + themeDto.getThemeName();
                    themeGroupMap.computeIfAbsent(compositeKey, k -> new ArrayList<>()).add(stock);
                }
            }
        }

        Map.Entry<String, List<StockHotResDto>> topTheme = findTopSingleGroup(themeGroupMap);
        String currentThemeName = "";
        if (topTheme != null) {
            String[] splitKey = topTheme.getKey().split("\\|\\|");
            currentThemeName = splitKey.length > 1 ? splitKey[1] : topTheme.getKey();
        }

        boolean isSectorChanged = !currentSectorName.equals(lastSectorName);
        boolean isThemeChanged = !currentThemeName.equals(lastThemeName);

        if (isSectorChanged || isThemeChanged) {
            this.lastSectorName = currentSectorName;
            this.lastThemeName = currentThemeName;

            StringBuilder alertMessage = new StringBuilder();
            alertMessage.append("📢 [시장 주도 흐름 변경 포착]\n\n");

            if (isSectorChanged && topSector != null) {
                alertMessage.append(formatLeaderMessage("🏢 주도 업종", currentSectorName, topSector.getValue())).append("\n");
            }
            if (isThemeChanged && topTheme != null) {
                alertMessage.append(formatLeaderMessage("🔥 주도 테마", currentThemeName, topTheme.getValue()));
            }

            telegramService.sendText("chatK", alertMessage.toString());
            telegramService.sendText("chatS", alertMessage.toString());

            return alertMessage.toString();
        }

        return "";
    }

    // 거래량 기반 타이브레이커 1등 추출
    private Map.Entry<String, List<StockHotResDto>> findTopSingleGroup(Map<String, List<StockHotResDto>> groupMap) {
        if (groupMap.isEmpty()) return null;

        return groupMap.entrySet().stream()
                .max((entry1, entry2) -> {
                    // 1순위 테마에 포착된 종목 수 비교
                    int sizeCompare = Integer.compare(entry1.getValue().size(), entry2.getValue().size());
                    if (sizeCompare != 0) {
                        return sizeCompare;
                    }

                    // 2순위 종목 수가 똑같은 같다면 해당 테마 종목들의 총 거래량을 합산하여 비교!
                    long vol1 = entry1.getValue().stream().mapToLong(StockHotResDto::getVolume).sum();
                    long vol2 = entry2.getValue().stream().mapToLong(StockHotResDto::getVolume).sum();
                    return Long.compare(vol1, vol2);
                })
                .orElse(null);
    }

    // 💡 대장주/2등주 포맷팅 로직 (선정된 테마 안에서만 경쟁)
    private String formatLeaderMessage(String title, String groupName, List<StockHotResDto> stocks) {
        stocks.sort(Comparator.comparingLong(StockHotResDto::getVolume).reversed()
            .thenComparing(Comparator.comparingDouble(StockHotResDto::getDiffRate).reversed()));

        StockHotResDto leader = stocks.get(0);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s: [%s] (관련 포착: %d개)\n", title, groupName, stocks.size()));
        sb.append(String.format(" 👑 대장주: %s (거래량: %,d / 등락: %+.2f%%)\n",
            leader.getStockName(), leader.getVolume(), leader.getDiffRate()));

        if (stocks.size() > 1) {
            for(int i=1; i<stocks.size(); i++) {
                StockHotResDto secondPlace = stocks.get(i);
                sb.append(String.format(" 🥈 " + (i+1) + "등주: %s (거래량: %,d / 등락: %+.2f%%)\n",secondPlace.getStockName(), secondPlace.getVolume(), secondPlace.getDiffRate()));
            }
        }

        return sb.toString();
    }
}
