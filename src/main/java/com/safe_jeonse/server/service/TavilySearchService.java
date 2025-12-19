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
            requestBody.put("max_results", 10);

            String response = restClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String answer = root.path("answer").asText();

            // 출처(URL) 정보 수집
            StringBuilder sources = new StringBuilder();
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode result : results) {
                    String title = result.path("title").asText();
                    String url = result.path("url").asText();
                    sources.append(String.format("- %s (%s)\n", title, url));
                }
            }

            if (sources.length() > 0) {
                log.info("Tavily 검색 출처 (쿼리: '{}'):\n{}", query, sources);
                return answer + "\n\n[참고 출처]\n" + sources.toString();
            }

            return answer;

        } catch (Exception e) {
            log.error("Tavily Search API 호출 실패", e);
            return "";
        }
    }
}
