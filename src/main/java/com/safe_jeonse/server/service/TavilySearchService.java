package com.safe_jeonse.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safe_jeonse.server.dto.response.AiApartmentPriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TavilySearchService {

    @Value("${spring.ai.tavily.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.tavily.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String search(String query) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("api_key", apiKey);
            requestBody.put("query", query);
            requestBody.put("search_depth", "advanced");
            requestBody.put("include_answer", true);
            requestBody.put("max_results", 5);

            // 네이버 부동산, 호갱노노, 아실, KB부동산 등 부동산 플랫폼 위주로 검색
            requestBody.put("include_domains", List.of(
                    "land.naver.com",
                    "hogangnono.com",
                    "asil.kr",
                    "kbland.kr"
            ));

            String response = restClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("answer").asText();

        } catch (Exception e) {
            log.error("Tavily Search API 호출 실패", e);
            return "";
        }
    }
}
