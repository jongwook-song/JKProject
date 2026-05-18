package com.jkproject.JkProject.util;


import com.jkproject.JkProject.stockHistory.StockHistoryConfig;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class TelegramService {
    private static String telegramToken;
    private static String telegramChatK;
    private static String telegramChatS;
    private static final Map<String, String> chatRooms = new ConcurrentHashMap<>();
    private static final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    public void setTelegramToken(String value) {
        telegramToken = value;
    }

    @Value("${telegram.chat.k}")
    public void setTelegramChatK(String value) {
        chatRooms.put("chatK", value);
        telegramChatK = value;
    }

    @Value("${telegram.chat.s}")
    public void setTelegramChatS(String value) {
        chatRooms.put("chatS", value);
        telegramChatS = value;
    }


    public void printToken(){
        System.out.println("telegramToken : " + telegramToken);
        System.out.println("telegramChatK : " + telegramChatK);
        System.out.println("telegramChatS : " + telegramChatS);
    }

    public void sendText(String roomName, String text) {
        try {
            System.out.println("text : " + text);
            String url = "https://api.telegram.org/bot" + telegramToken + "/sendMessage";
            Map<String, String> payload = Map.of("chat_id", chatRooms.get(roomName), "text", text, "parse_mode", "HTML");

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(StockHistoryConfig.MAPPER.writeValueAsString(payload))).build();

            HttpResponse<String> response = StockHistoryConfig.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            handleMigration(roomName, response);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void sendPdf(String roomName, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try {
            String url = "https://api.telegram.org/bot" + telegramToken + "/sendDocument";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("chat_id", chatRooms.get(roomName));
            body.add("document", new FileSystemResource(file));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ 텔레그램 리포트 전송 성공!");
            } else {
                System.err.println("❌ 전송 실패: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("❗ 텔레그램 전송 중 오류: " + e.getMessage());
        } finally {
            if (file.exists() && roomName.equals("chatS")) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("🗑️ 서버 임시 PDF 파일 삭제 완료.");
                }
            }
        }
    }

    public void sendImage(String roomName, String stockCode) {
        File file = new File("./" + stockCode + ".png");
        if (!file.exists()) return;

        try {
            String url = "https://api.telegram.org/bot" + telegramToken + "/sendPhoto";
            String boundary = "---TelegramBoundary" + System.currentTimeMillis();
            byte[] fileBytes = Files.readAllBytes(Path.of(file.getAbsolutePath()));

            String headerInfo = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n" + chatRooms.get(roomName) + "\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"photo\"; filename=\"chart.png\"\r\n" +
                "Content-Type: image/png\r\n\r\n";
            String footerInfo = "\r\n--" + boundary + "--\r\n";

            byte[] header = headerInfo.getBytes("UTF-8");
            byte[] footer = footerInfo.getBytes("UTF-8");
            byte[] body = new byte[header.length + fileBytes.length + footer.length];

            System.arraycopy(header, 0, body, 0, header.length);
            System.arraycopy(fileBytes, 0, body, header.length, fileBytes.length);
            System.arraycopy(footer, 0, body, header.length + fileBytes.length, footer.length);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();

            HttpResponse<String> response = StockHistoryConfig.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            handleMigration(roomName, response);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleMigration(String roomName, HttpResponse<String> response) {
        if (response.body().contains("migrate_to_chat_id")) {
            String newId = response.body().split("\"migrate_to_chat_id\":")[1].split("}")[0].trim();
            chatRooms.put(roomName, newId);
            System.out.println("🔄 " + roomName + " 방 ID 마이그레이션: " + newId);
        }
    }

    public String formatAnalystReport(String originalText) {
        if (originalText == null) return "";

        String formatted = originalText
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");

        formatted = formatted.replaceAll("(?m)^###\\s*(.*)$", "<b>$1</b>");
        formatted = formatted.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        formatted = formatted.replace("* ", "• ");
        formatted = formatted.replace("---", "━━━━━━━━━━━━━━━━━━");
        formatted = formatted.replace("[", "【").replace("]", "】");

        return formatted;
    }
}