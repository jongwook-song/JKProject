package com.jkproject.JkProject.stockHistory;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkproject.JkProject.stockHistory.dto.StockHotResDto;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockHistoryConfig {
    public static final String REST_URL = "https://api.kiwoom.com";
    public static final String SOCKET_URL = "wss://api.kiwoom.com:10000/api/dostk/websocket"; // 접속 URL

    public static String accessToken = ""; // 발급받은 토큰 저장용
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static Map<String, String> STOCK_MAP = new HashMap<String, String>();
    public static Map<Long, String> THEME_MAP = new HashMap<Long, String>();
    public static final Map<String, StockHotResDto> detectedStocksMap = new ConcurrentHashMap<>();
}