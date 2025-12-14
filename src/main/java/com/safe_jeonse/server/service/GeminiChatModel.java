package com.safe_jeonse.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gemini API를 위한 커스텀 ChatModel 구현
 */
@Slf4j
public class GeminiChatModel implements ChatModel {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiChatModel(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        int maxRetries = 3;
        long waitTime = 2000; // 초기 대기 시간 2초

        for (int i = 0; i <= maxRetries; i++) {
            try {
                // Gemini API 요청 형식으로 변환 (Google Search 포함)
                Map<String, Object> requestBody = buildGeminiRequest(prompt);

                String requestJson = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(requestBody);

                // Gemini API 호출
                String response = restClient.post()
                        .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);


                // 응답 파싱
                ChatResponse chatResponse = parseGeminiResponse(response);

                return chatResponse;

            } catch (Exception e) {
                // 429 에러 체크 (Rate Limit)
                if (isRateLimitError(e) && i < maxRetries) {
                    log.warn("Gemini API 429 Too Many Requests 발생. {}ms 후 재시도합니다. (시도 {}/{})",
                            waitTime, i + 1, maxRetries);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                    waitTime *= 2; // 지수 백오프 (2초 -> 4초 -> 8초)
                    continue;
                }

                log.error("Gemini API 호출 실패", e);
                throw new RuntimeException("Gemini API 호출 중 오류 발생: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Gemini API 호출 실패: 최대 재시도 횟수 초과");
    }

    private boolean isRateLimitError(Exception e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("429") || msg.contains("Too Many Requests") || msg.contains("quota"));
    }

    /**
     * Spring AI Prompt를 Gemini API 형식으로 변환
     * Google Search Grounding 기능 포함
     */
    private Map<String, Object> buildGeminiRequest(Prompt prompt) {
        StringBuilder combinedText = new StringBuilder();

        for (Message message : prompt.getInstructions()) {
            String content = message.getText();
            if (content != null && !content.isEmpty()) {
                combinedText.append(content).append("\n\n");
            }
        }

        Map<String, Object> part = new HashMap<>();
        part.put("text", combinedText.toString().trim());

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(content));

        // ★ Google Search 기능 활성화
        Map<String, Object> googleSearch = new HashMap<>();

        Map<String, Object> tool = new HashMap<>();
        tool.put("google_search", googleSearch);

        request.put("tools", List.of(tool));

        return request;
    }

    /**
     * Gemini API 응답을 Spring AI ChatResponse로 변환
     */
    private ChatResponse parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Gemini 응답 구조: candidates[0].content.parts[0].text
        String text = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText("");

        AssistantMessage message = new AssistantMessage(text);
        Generation generation = new Generation(message);

        return new ChatResponse(List.of(generation));
    }
}
