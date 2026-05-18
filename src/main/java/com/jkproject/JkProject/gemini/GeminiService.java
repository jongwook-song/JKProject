package com.jkproject.JkProject.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkproject.JkProject.util.PdfService;
import com.jkproject.JkProject.util.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private TelegramService telegramService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Async
    public CompletableFuture<String> generateMarketCommentary(String prompt, String title) {
        String model = "gemini-2.5-flash"; // 빠르고 저렴한 모델
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        try {
//            System.out.println("prompt : " + prompt);
            String jsonPayload = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", prompt.replace("\"", "\\\""));
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = mapper.readTree(response.body());
                String aiText = rootNode.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText().replaceAll("```html|```", "").trim();
//                System.out.println(aiText);
                String pdfPath = PdfService.createMarketReport(aiText, title);

                telegramService.sendPdf("chatK", pdfPath);
                telegramService.sendPdf("chatS", pdfPath);

                return CompletableFuture.completedFuture(aiText);
            } else {
                System.err.println("제미나이 API 에러: " + response.body());
                return CompletableFuture.completedFuture("");
            }
        } catch (Exception e) {
            System.err.println("제미나이 호출 중 오류: " + e.getMessage());
            return CompletableFuture.completedFuture("");
        }
    }
}