package com.safe_jeonse.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safe_jeonse.server.dto.response.AiApartmentPriceResponse;
import com.safe_jeonse.server.dto.response.MarketPrice;
import com.safe_jeonse.server.exception.AiApiException;
import com.safe_jeonse.server.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * 아파트 실거래가 조회 로직 (AI 기반)
 * 추후 로직 변경
 */
@Slf4j
@Service
public class AptAnalysisService {

    private final ChatModel chatModel;
    private final PromptManager promptManger;
    private final TavilySearchService tavilySearchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AptAnalysisService(
            @Qualifier("groqChatModel") ChatModel chatModel,
            PromptManager promptManger,
            TavilySearchService tavilySearchService) {
        this.chatModel = chatModel;
        this.promptManger = promptManger;
        this.tavilySearchService = tavilySearchService;
    }

    /**
     * AI를 통한 아파트 가격 조회
     *
     * @param address 주소
     * @param apartmentName 아파트명
     * @param exclusiveArea 전용면적
     * @return 시장가격 정보
     */
    public MarketPrice getAptPrice(String address, String apartmentName, String exclusiveArea) {

        // 아파트명이나 전용면적이 없으면 조회 불가
        if (apartmentName == null || apartmentName.isEmpty() ||
                exclusiveArea == null || exclusiveArea.isEmpty()) {
            log.warn("아파트명 또는 전용면적 정보 없음 - AI 조회 불가");
            return MarketPrice.builder()
                    .marketPrice(0L)
                    .info("APARTMENT - 정보 부족 (AI 조회 불가)")
                    .build();
        }

        // 1. Tavily 검색 수행
        // 쿼리 강화: "매매" 강조, "상한가 하한가" 추가로 시세표 유도
        String searchQuery = String.format("%s %s 전용면적 %s 매매 시세 실거래가 (전세 제외)", address, apartmentName, exclusiveArea);
        String searchResult = tavilySearchService.search(searchQuery);
        log.info("검색중:{}", searchQuery);
        if (searchResult == null || searchResult.isEmpty()) {
            log.warn("Tavily 검색 결과 없음");
            searchResult = "검색 결과 없음";
        }

        // 2. AI에게 검색 결과 분석 및 JSON 변환 요청
        String systemPrompt = promptManger.getAptPriceSystemPrompt();

        // 기존 프롬프트
        String instructionPrompt = promptManger.getAptPricePrompt(address, apartmentName, exclusiveArea);

        // 최종 프롬프트
        String userPrompt = String.format("""
                %s
                
                ---
                [참고: 웹 검색 결과]
                다음은 위 정보를 찾기 위해 수행한 Tavily 검색 결과입니다. 
                아래 검색 결과를 바탕으로 위 지침에 따라 분석을 수행해주세요.
                
                %s
                """, instructionPrompt, searchResult);

        log.info("AI 아파트 가격 조회 시작: {} - {}, {}㎡", address, apartmentName, exclusiveArea);

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)));

            ChatResponse response = chatModel.call(prompt);
            log.info("result:{}", response);
            // Spring AI의 응답에서 텍스트 추출
            String aiResponse = response.getResult().getOutput().getText();

            // JSON 파싱 시도
            AiApartmentPriceResponse priceData = parseAiResponse(aiResponse);

            // 데이터 검증
            if (priceData.hasNoData()) {
                log.warn("AI가 거래 데이터 없음을 보고함");
                return MarketPrice.builder()
                        .marketPrice(0L)
                        .info("APARTMENT - AI: 거래 데이터 없음")
                        .build();
            }

            // 신뢰도 검증
            if (!priceData.isReliable()) {
                log.warn("AI 응답 신뢰도 낮음: {}", priceData.confidence());
                return MarketPrice.builder()
                        .marketPrice(priceData.averagePrice())
                        .info(String.format("APARTMENT - AI: 신뢰도 낮음 (%s, %d건)",
                                priceData.confidence(), priceData.transactionCount()))
                        .build();
            }

            // 정상 응답
            String info = String.format("APARTMENT - AI: %s %s㎡ (평균 %d건, 신뢰도: %s, 출처: %s)",
                    apartmentName,
                    exclusiveArea,
                    priceData.transactionCount(),
                    priceData.confidence(),
                    priceData.dataSource());

            log.info("AI 아파트 가격 조회 완료: {}원 - {}", priceData.averagePrice(), info);

            return MarketPrice.builder()
                    .marketPrice(priceData.averagePrice())
                    .info(info)
                    .build();

        } catch (Exception e) {
            log.error("AI 아파트 가격 조회 중 오류 발생: {}", address, e);
            throw new AiApiException("AI를 통한 아파트 가격 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * AI 응답을 JSON으로 파싱
     * AI가 다양한 형식으로 응답할 수 있으므로 견고하게 처리
     */
    private AiApartmentPriceResponse parseAiResponse(String aiResponse) {
        try {
            if (aiResponse == null || aiResponse.isEmpty()) {
                log.error("AI 응답이 비어있음");
                return createEmptyResponse("AI 응답 없음");
            }

            log.debug("원본 AI 응답: {}", aiResponse);

            // JSON 부분만 추출 (마크다운 코드블록 제거)
            String jsonStr = extractJson(aiResponse);

            log.debug("추출된 JSON: {}", jsonStr);

            // 빈 JSON 체크
            if (jsonStr == null || jsonStr.isEmpty() || jsonStr.equals("{}")) {
                log.error("추출된 JSON이 비어있음. 원본: {}", aiResponse);
                return createEmptyResponse("JSON 추출 실패");
            }

            // Jackson으로 파싱
            AiApartmentPriceResponse response = objectMapper.readValue(jsonStr, AiApartmentPriceResponse.class);

            log.info("파싱 성공 - averagePrice: {}, confidence: {}",
                    response.averagePrice(), response.confidence());

            return response;

        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            log.error("JSON 파싱 오류 - 유효하지 않은 JSON 형식. 원본: [{}], 에러: {}",
                    aiResponse, e.getMessage());
            return createEmptyResponse("잘못된 JSON 형식: " + e.getMessage());
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            log.error("JSON 매핑 오류 - 필드 구조 불일치. 원본: [{}], 에러: {}",
                    aiResponse, e.getMessage());
            return createEmptyResponse("JSON 구조 불일치: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI 응답 파싱 중 예상치 못한 오류. 원본: [{}], 에러: {}",
                    aiResponse, e.getMessage(), e);
            return createEmptyResponse("파싱 실패: " + e.getMessage());
        }
    }

    /**
     * AI 응답에서 JSON 문자열만 추출
     */
    private String extractJson(String aiResponse) {
        String cleaned = aiResponse.trim();

        // 케이스 1: ```json ... ``` 형식
        if (cleaned.contains("```json")) {
            int start = cleaned.indexOf("```json") + 7;
            int end = cleaned.indexOf("```", start);
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        }
        // 케이스 2: ``` ... ``` 형식
        else if (cleaned.startsWith("```")) {
            int start = cleaned.indexOf("```") + 3;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
                // json 키워드가 시작부분에 있을 수 있음
                if (cleaned.toLowerCase().startsWith("json")) {
                    cleaned = cleaned.substring(4).trim();
                }
            }
        }

        // 케이스 3: JSON 앞뒤에 설명 텍스트가 있는 경우
        // { 로 시작하는 첫 위치부터 마지막 } 까지 추출
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        log.debug("최종 추출된 JSON 문자열: {}", cleaned);

        return cleaned;
    }

    /**
     * 파싱 실패 시 빈 응답 생성
     */
    private AiApartmentPriceResponse createEmptyResponse(String reason) {
        return AiApartmentPriceResponse.builder()
                .averagePrice(0L)
                .confidence("none")
                .dataSource(reason)
                .transactionCount(0)
                .priceRange(new AiApartmentPriceResponse.PriceRange(0L, 0L))
                .note("데이터를 가져올 수 없습니다")
                .build();
    }

}