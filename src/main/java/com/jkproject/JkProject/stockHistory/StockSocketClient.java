package com.jkproject.JkProject.stockHistory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jkproject.JkProject.gemini.GeminiService;
import com.jkproject.JkProject.stock.Stock;
import com.jkproject.JkProject.stock.StockService;
import com.jkproject.JkProject.stockTheme.StockThemeMapService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.jkproject.JkProject.stockHistory.StockHistoryService.conditionMap;

public class StockSocketClient extends WebSocketClient {

    private final String accessToken;
    private final StockHistoryService stockHistoryService;
    private final StockService stockService;
    private final StockThemeMapService stockThemeMapService;
    private final GeminiService geminiService;

    private static final Gson GSON = new Gson();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] CONDITION_SEQS = {"7", "15", "16", "13"};

    private final Map<String, String> stockStatusMap = new ConcurrentHashMap<>();
    private String lastStock = "";

    public StockSocketClient(URI serverUri, String accessToken, StockService stockService, StockHistoryService stockHistoryService, StockThemeMapService stockThemeMapService, GeminiService geminiService) {

        super(serverUri);
        this.accessToken = accessToken;
        this.stockService = stockService;
        this.stockHistoryService = stockHistoryService;
        this.stockThemeMapService = stockThemeMapService;
        this.geminiService = geminiService;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("✅ 서버와 소켓 연결이 완료되었습니다.");

        JsonObject loginMsg = new JsonObject();
        loginMsg.addProperty("trnm", "LOGIN");
        loginMsg.addProperty("token", accessToken);

        System.out.println("📡 로그인 인증 요청 중...");
        sendMessage(loginMsg);
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject response = GSON.fromJson(message, JsonObject.class);
            String trnm = response.has("trnm") ? response.get("trnm").getAsString() : "";

            if ("LOGIN".equals(trnm)) {
                String returnCode = response.get("return_code").getAsString();
                if ("0".equals(returnCode)) {
                    System.out.println("🎊 로그인 인증 성공! 실시간 조건검색 등록을 시작합니다.");

                    JsonObject conditionListRequest = new JsonObject();
                    conditionListRequest.addProperty("trnm", "CNSRLST");
                    sendMessage(conditionListRequest);

                    registerAllConditions();
                } else {
                    System.err.println("❌ 로그인 실패: " + response.get("return_msg").getAsString());
                    this.close();
                }
            } else if ("PING".equals(trnm)) {
                sendMessage(response);
            }

            if (!"PING".equals(trnm)) {
//                System.out.println("message : " + message);
                processRealTime(message);
            }
        } catch (Exception e) {
            System.err.println("❗ 메시지 처리 중 오류 발생: " + e.getMessage());
        }
    }

    private void registerAllConditions() {
        new Thread(() -> {
            try {
                for (String seq : CONDITION_SEQS) {
                    JsonObject regMsg = new JsonObject();
                    regMsg.addProperty("trnm", "CNSRREQ");
                    regMsg.addProperty("seq", seq);
                    regMsg.addProperty("search_type", "1");
                    regMsg.addProperty("stex_tp", "K");

                    JsonObject dataObj = new JsonObject();
                    dataObj.add("item", new JsonArray());
                    dataObj.add("type", new JsonArray());
                    regMsg.add("data", GSON.toJsonTree(new JsonObject[]{dataObj}));

                    sendMessage(regMsg);
                    System.out.println("📡 [조건검색 등록 요청 발송] 일련번호: " + seq);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processRealTime(String jsonString) {
        try {
            JsonNode root = MAPPER.readTree(jsonString);
            JsonNode dataArray = root.path("data");

            if(root.path("trnm").asText().equals("CNSRREQ") && (root.path("seq").asText().equals("16") || root.path("seq").asText().equals("17"))){
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//                String formatTime = String.format("%s:%s:%s",time.substring(0, 2), time.substring(2, 4), time.substring(4, 6));
                LocalTime now = LocalTime.now();
                LocalTime targetTime = LocalTime.of(15, 40);
                String stockCode = "";
                Stock stockMaster = null;
                JsonNode stockCodeArry = root.path("data");

                if(stockCodeArry.isArray() && stockCodeArry.size() > 0){
                    for(int i=0; i<stockCodeArry.size(); i++){
                        stockCode = stockCodeArry.get(i).path("jmcode").asText().replace("A", "");

                        stockMaster = stockService.stockFindByid(stockCode);
                        if (now.isBefore(targetTime)) {
                            stockHistoryService.analyzeHotStock(stockCode, stockMaster, root.path("seq").asText(), time);
                        }
                    }
                }
            }
            else if(root.path("trnm").asText().equals("CNSRREQ") && (root.path("seq").asText().equals("6") || root.path("seq").asText().equals("9"))) {
                String stockCode = "";
                String prompt = "";
                String title = "";
                JsonNode stockCodeArry = root.path("data");
                List<String> stockNames = new ArrayList<String>();

                for(int i=0; i<stockCodeArry.size(); i++){
                    stockCode = stockCodeArry.get(i).path("jmcode").asText().replace("A", "");

                    Stock stockMaster = stockService.stockFindByid(stockCode);
                    stockNames.add(stockMaster.getName());
                }

                if(root.path("seq").asText().equals("6")){
                    prompt =  "# Role\n" +
                            "너는 기관 투자자들을 대상으로 '기술적 수급 분석'과 '섹터 로테이션'을 전문적으로 다루는 수석 에쿼티 애널리스트야. \n" +
                            "오늘 거래량이 폭발한 " + stockNames.size() + "개 종목 [ " + String.join(", ", stockNames) + " ]을 실시간 검색하여 심층 분석 리포트를 작성해.\n" +
                            "\n" +
                            "# Objective\n" +
                            "제공된 " + stockNames.size() + "개 종목의 기술적 지표와 최신 뉴스를 융합하여 HTML 형식의 리포트를 생성해. \n" +
                            "서론 없이 바로 종목별 분석 본론으로 들어갈 것.\n" +
                            "\n" +
                            "# Style & Formatting (For PDF Conversion)\n" +
                            "- 모든 응답은 <div>, <table>, <span> 태그를 사용하여 구조화해.\n" +
                            "- 전문적인 유료 리포트 느낌의 인라인 CSS(style)를 적용해. (예: 섹터별 구분 선, 강조 박스 등)\n" +
                            "- 폰트 가독성을 위해 줄 간격(line-height: 1.6)과 충분한 여백(padding)을 설정해.\n" +
                            "- 금융 전문 용어(Golden Cross, RSI Divergence, CAPEX, Short-covering 등)를 적극 사용해.\n" +
                            "\n" +
                            "# Content Requirements (종목별 공통 적용)\n" +
                            "1. Technical Analysis:\n" +
                            "   - 현재 거래량 증가가 '매집형'인지 '차익실현형'인지 주가 상승과의 상관관계 분석.\n" +
                            "   - 이동평균선(MA), RSI, 볼린저 밴드 등 기술적 지표를 바탕으로 한 상승 모멘텀의 지속성 평가.\n" +
                            "\n" +
                            "2. Theme & News Context:\n" +
                            "   - 해당 종목의 주가를 움직인 최신 개별 공시, 뉴스, 혹은 속한 테마의 글로벌 트렌드 정리.\n" +
                            "   - 장중 수급에 영향을 미친 핵심 키워드 추출.\n" +
                            "\n" +
                            "3. Peer Group Analysis:\n" +
                            "   - 해당 종목과 비즈니스 모델이나 테마 노출도가 80% 이상 유사한 '유사 종목' 2~3개 추천.\n" +
                            "   - 추천 이유와 함께 대장주 이동 가능성(순환매) 분석.\n" +
                            "\n" +
                            "4. Investment Strategy (Opinion):\n" +
                            "   - 현재 주가 위치가 '단기 과열(Overheated)' 영역인지, 아니면 바닥권에서의 '추세 전환(Trend Reversal)' 시점인지 명확한 의견 제시.\n" +
                            "   - 손절가(Stop-loss) 및 단기 목표가(Target Price)에 대한 기술적 가이드 포함.\n" +
                            "\n" +
                            "# Constraint\n" +
                            "- 각 종목은 독립된 <div style=\"border: 1px solid #ddd; padding: 20px; margin-bottom: 20px;\"> 박스 안에 담아줘.\n" +
                            "- 마지막에 표준 [면책 조항]을 포함할 것.";
                    title = "거래량 급증";
                }
                else if(root.path("seq").asText().equals("9")) {
                    prompt = "# Role\n" +
                            "너는 대형 자산운용사의 '수급 전략 팀장'이자 '퀀트 분석가'야. \n" +
                            "최근 3거래일 연속 외국인과 기관의 강력한 양매수(쌍끌이)가 유입된 4개 종목 [" + String.join(", ", stockNames) + "]을 실시간 검색하여 수급 밀착 분석 리포트를 작성해.\n" +
                            "\n" +
                            "# Objective\n" +
                            "제공된 " + stockNames.size() + "개 종목의 수급 데이터와 펀더멘털 뉴스를 융합하여 HTML 형식의 리포트를 생성해. \n" +
                            "서론 없이 바로 분석 본론으로 들어갈 것.\n" +
                            "\n" +
                            "# Style & Formatting (For Professional PDF)\n" +
                            "- 모든 응답은 <div>, <table>, <span> 태그를 사용하여 구조화해.\n" +
                            "- 전문적인 유료 리포트 느낌의 인라인 CSS(style)를 적용해. (예: 수급 강도 표시 바, 강조 박스 등)\n" +
                            "- 금융 전문 용어(Net Buying Intensity, Forward P/E, EPS Growth, Peer Multiple 등)를 적극 사용해.\n" +
                            "- 종목명 '아크릴'의 경우 정확한 상장사명(예: 아스플로 등 유사명칭 포함)을 검색을 통해 확인 후 분석할 것.\n" +
                            "\n" +
                            "# Content Requirements\n" +
                            "1. Flow Analysis (수급의 질 분석):\n" +
                            "   - 최근 3일간 유입된 외국인/기관의 매수 강도(유통주식수 대비 비중) 분석.\n" +
                            "   - 단순 매수를 넘어 '패시브 자금'인지 '액티브 자금'인지, 혹은 '숏커버링' 성격인지에 대한 수급의 질 평가.\n" +
                            "\n" +
                            "2. Fundamental & News (수급 개선 촉매):\n" +
                            "   - 수급 주체들이 갑자기 이 종목을 선택하게 만든 매크로 환경(환율, 원자재가 등)이나 섹터별 핵심 뉴스 분석.\n" +
                            "   - 최근 공시된 실적이나 정부 정책이 수급 유입에 미친 영향 기술.\n" +
                            "\n" +
                            "3. Comparative Valuation (선정 이유 및 대안):\n" +
                            "   - 동일 섹터 내 타 종목 대비 이 종목이 가진 밸류에이션 매력(P/E, P/B 가이드) 분석.\n" +
                            "   - 해당 종목 외에 섹터 내에서 유사한 수급 패턴을 보이기 시작한 '대안 종목' 1개 제시.\n" +
                            "\n" +
                            "4. Peer Group Analysis (유사 종목 추천):\n" +
                            "   - 비즈니스 모델이나 테마 노출도가 85% 이상 유사하여 '동반 상승(Couple-play)'이 가능한 유사 종목 2~3개 추천.\n" +
                            "   - 각 추천 종목별 선정 이유와 수급 전이 가능성 분석.\n" +
                            "\n" +
                            "5. Analyst View (수급 연속성 판단):\n" +
                            "   - 이 매수세가 일시적 이벤트인지, 아니면 중장기 우상향을 위한 매집인지에 대한 최종 판단.\n" +
                            "   - 수급 연속성을 보장할 수 있는 핵심 트리거(실적 발표일, 지수 편입 여부 등) 제시.\n" +
                            "\n" +
                            "# Constraint\n" +
                            "- 각 종목별 분석은 세련된 <div style=\"background-color: #f8fafc; border-radius: 8px; padding: 15px; margin-bottom: 20px;\"> 박스 모델을 사용해.\n" +
                            "- 마지막에 표준 [면책 조항]을 포함할 것.";
                    title = "외인 및 기관 3일 매수";
                }
                geminiService.generateMarketCommentary(prompt, title);

//                registerSingleCondition(root.path("seq").asText());
            }
            else if (dataArray.isArray() && dataArray.size() > 0) {
                JsonNode values = dataArray.get(0).path("values");
                String stockCode = values.path("9001").asText();
                String status = values.path("843").asText();
                String time = values.path("20").asText();

                String lastStatus = stockStatusMap.getOrDefault(stockCode, "");

                if (lastStock.equals("") || !stockCode.equals(lastStock) && "I".equals(status) && !"I".equals(lastStatus)) {
                    Stock stockMaster = stockService.stockFindByid(stockCode);

                    if (stockMaster != null) {
                        String formatTime = String.format("%s:%s:%s",time.substring(0, 2), time.substring(2, 4), time.substring(4, 6));
                        System.out.println("🚀 [ " + conditionMap.get(values.path("841").asText()) + " | " + stockMaster.getName() + "] 포착! 분석 스레드로 전달합니다.");

                        if(values.path("841").asText().equals(("16")) || values.path("841").asText().equals(("15"))){
                            stockHistoryService.analyzeHotStock(stockCode, stockMaster, values.path("841").asText(), time);
                        }
                        else{
                            stockHistoryService.processAsync(
                                stockCode,
                                stockMaster.getName(),
                                stockMaster.getType() + " | " + stockMaster.getDetailType(),
                                formatTime,
                                values.path("841").asText()
                            );
                        }

                    } else {
                        System.err.println("⚠️ DB에 존재하지 않는 종목 코드입니다: " + stockCode);
                    }
                }
                lastStock = stockCode;
                stockStatusMap.put(stockCode, status);
            }
        } catch (Exception e) {
            System.err.println("❗ 실시간 데이터 파싱 에러: " + e.getMessage());
        }
    }

    public void sendMessage(JsonObject message) {
        if (this.isOpen()) {
            this.send(GSON.toJson(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("🔌 웹소켓 연결이 종료되었습니다. 사유: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("⚠️ 웹소켓 에러 발생:");
        ex.printStackTrace();
    }

    public void registerSingleCondition(String conditionSeq) {
//        detectedStocksMap.clear();
//        stockStatusMap.clear();
//        lastStock = "";

        JsonObject request = new JsonObject();
        request.addProperty("trnm", "CNSRREQ");
        request.addProperty("seq", conditionSeq);
        request.addProperty("search_type", "1");
        request.addProperty("stex_tp", "K");
        request.addProperty("cont_yn", "N");
        request.addProperty("next_key", "");

        sendMessage(request);
        System.out.println("✅ 조건식(seq:" + conditionSeq + ") 실시간 등록 완료!");
    }

    public void unregisterCondition(String conditionSeq) {
        JsonObject request = new JsonObject();
        request.addProperty("trnm", "CNSRCLR");
        request.addProperty("seq", conditionSeq);
        request.addProperty("stex_tp", "K");
        sendMessage(request);
        System.out.println("⛔ [" + conditionSeq + "] 조건식 실시간 해제 완료!");
    }
}