package com.jkproject.JkProject.stockHistory;

import com.jkproject.JkProject.gemini.GeminiService;
import com.jkproject.JkProject.stock.StockService;
import com.jkproject.JkProject.stockHistory.dto.StockHistoryResDto;
import com.jkproject.JkProject.stockTheme.StockThemeMapService;
import com.jkproject.JkProject.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jkproject.JkProject.stockHistory.StockHistoryConfig.accessToken;

@RestController
@RequiredArgsConstructor
public class StockHistoryController {
    private final StockHistoryService stockHistoryService;
    private final StockService stockService;
    private final StockThemeMapService stockThemeMapService;
    private final GeminiService geminiService;

    private StockSocketClient socketClient;

    @GetMapping("/getToken")
    public String getAccessToken() {
        try {
            if (accessToken.equals("")) {
                accessToken = stockHistoryService.getAccessToken();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ("실행 중 오류 발생: " + e.getMessage());
        }

        return accessToken.equals("") ? "error" : "ok";
    }

    @GetMapping("/tokenCheck")
    public String tokenCHeck() {
        System.out.println("accessToken : " + accessToken);

        return accessToken.equals("") ? "error" : "ok";
    }
    @PostMapping("/start")
    public ResponseEntity<String> startTrading() {
        try {
            if (socketClient != null && socketClient.isOpen()) {
                return ResponseEntity.badRequest().body("이미 트레이딩 봇이 실행 중입니다.");
            }

            if(accessToken.equals("")){
                accessToken = stockHistoryService.getAccessToken();
            }

            if(accessToken.equals("")){
                return ResponseEntity.internalServerError().body("실행 중 오류 발생 : 토큰 Null");
            }

            socketClient = new StockSocketClient(new URI(StockHistoryConfig.SOCKET_URL), accessToken, stockService, stockHistoryService, stockThemeMapService, geminiService);
            socketClient.connect();

            return ResponseEntity.ok("트레이딩 봇 웹소켓 연결이 시작되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("실행 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopTrading() {
        if (socketClient != null && !socketClient.isClosed()) {
            socketClient.close();
            socketClient = null;
            return ResponseEntity.ok("🛑 트레이딩 봇 웹소켓 통신을 안전하게 종료했습니다.");
        }

        return ResponseEntity.ok("⚠️ 현재 실행 중인 트레이딩 봇이 없습니다.");
    }

    @RequestMapping("/valueTest")
    public String valueTest(){
        return "valueTest";
    }

    @GetMapping("/history/load")
    public List<StockHistoryResDto> getStockHistory(@RequestParam(name = "page", defaultValue = "0") int page) {
        return stockHistoryService.getStockHistory(0);
    }

    @GetMapping("/gemini/today/hot")
    public void todayHot () {
        socketClient.registerSingleCondition("6");
    }

    @GetMapping("/gemini/today/buy")
    public void todayBuy () {
        socketClient.registerSingleCondition("9");
    }

    @MessageMapping("/condition/start")
    public void startCondition(@RequestParam Map<String,Object> paramMap) {
        HashMap<String, String> res = new HashMap<>();
        res.put("conditionSeq", ObjectUtil.getMapVal(paramMap, "conditionSeq"));

        System.out.println("프론트엔드에서 조건식 실행 요청 들어옴: " + res.get("conditionSeq"));

        socketClient.registerSingleCondition(res.get("conditionSeq"));
    }

    @MessageMapping("/condition/end")
    public void endCondition(@RequestParam Map<String,Object> paramMap) {
        HashMap<String, String> res = new HashMap<>();
        res.put("conditionSeq", ObjectUtil.getMapVal(paramMap, "conditionSeq"));

        System.out.println("프론트엔드에서 조건식 중단 요청 들어옴: " + res.get("conditionSeq"));

        socketClient.unregisterCondition(res.get("conditionSeq"));
    }
}
