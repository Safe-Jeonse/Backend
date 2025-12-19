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

        // 1. Tavily 검색 수행 (1회만 호출)
        String searchResult = performSearch(address, apartmentName, exclusiveArea);

        if (searchResult == null || searchResult.isEmpty()) {
            log.warn("Tavily 검색 결과 없음");
            return MarketPrice.builder().marketPrice(0L).info("검색 결과 없음").build();
        }

        // 2. AI 분석 수행 (1회만 호출)
        return analyzeWithAi(searchResult, address, apartmentName, exclusiveArea);
    }

    private String performSearch(String address, String apartmentName, String exclusiveArea) {
        String addressWithDong = address.replaceAll("\\d+호", "").trim();

        // 전용면적 정수화 및 시장 통용 평형 환산
        String roundedArea = exclusiveArea;
        String marketPyeong = "";
        try {
            double area = Double.parseDouble(exclusiveArea);
            roundedArea = String.valueOf((int) area);

            // 전용면적 → 시장 통용 평형 변환 (공급면적 기준)
            // 실제 시장에서는 공용면적 포함한 '공급면적' 기준으로 평형 표기
            marketPyeong = convertToMarketPyeong((int) area);

        } catch (Exception e) {
            // 변환 실패 시 원래 값 사용
        }

        // 시장 통용 평형을 포함하여 검색 정확도 향상
        String query1 = String.format("%s %s %s㎡ %s평형 매매",
                addressWithDong, apartmentName, roundedArea, marketPyeong);

        log.info("1차 검색 시도 (전용{}㎡ = 시장{}평형): {}", roundedArea, marketPyeong, query1);
        String result1 = tavilySearchService.search(query1);

        log.info("1차 검색 결과 길이: {} (참고출처 포함)", result1 != null ? result1.length() : 0);

        // 결과가 충분하면 반환
        if (result1 != null && result1.length() > 100) {
            return result1;
        }
        else {
            return null;
        }

    }

    /**
     * 전용면적(㎡)을 시장에서 통용되는 평형으로 변환
     * 실제 부동산 시장에서는 공급면적 기준으로 평형을 표기
     *
     * 예시: 84㎡ 전용면적 = "34평형" (공급면적 약 112㎡ ≈ 34평)
     *
     * @param exclusiveArea 전용면적 (㎡)
     * @return 시장 통용 평형 (예: 84㎡ → "34평")
     */
    private String convertToMarketPyeong(int exclusiveArea) {
        // 주요 평형대 매핑 (전용면적 → 시장 통용 평형)
        // 공급면적 = 전용면적 ÷ 전용률 (일반적으로 60~75%, 평균 70%)

        // 국민 평수 (가장 흔한 케이스)
        if (exclusiveArea >= 82 && exclusiveArea <= 86) {
            return "34평"; // 84㎡, 85㎡ 이하 세금 혜택
        }
        // 중소형
        else if (exclusiveArea >= 58 && exclusiveArea <= 62) {
            return "25평"; // 59㎡
        }
        else if (exclusiveArea >= 48 && exclusiveArea <= 52) {
            return "20평"; // 49㎡
        }
        else if (exclusiveArea >= 38 && exclusiveArea <= 42) {
            return "16평"; // 39㎡
        }
        // 중형
        else if (exclusiveArea >= 73 && exclusiveArea <= 78) {
            return "32평"; // 74㎡
        }
        else if (exclusiveArea >= 98 && exclusiveArea <= 102) {
            return "40평"; // 99㎡
        }
        // 대형
        else if (exclusiveArea >= 114 && exclusiveArea <= 120) {
            return "45평"; // 114㎡
        }
        else if (exclusiveArea >= 125 && exclusiveArea <= 135) {
            return "49평"; // 128㎡
        }
        else if (exclusiveArea >= 145 && exclusiveArea <= 155) {
            return "59평"; // 149㎡
        }
        else {
            // 매핑되지 않은 면적은 대략적인 공급면적 환산
            // 전용률 70% 가정: 공급면적 = 전용면적 ÷ 0.7
            int supplyArea = (int) (exclusiveArea / 0.7);
            int pyeong = (int) (supplyArea / 3.3);
            return pyeong + "평";
        }
    }

    private MarketPrice analyzeWithAi(String searchResult, String address, String apartmentName, String exclusiveArea) {
        // AI에게 검색 결과 분석 요청
        String systemPrompt = promptManger.getAptPriceSystemPrompt();
        String instructionPrompt = promptManger.getAptPricePrompt(address, apartmentName, exclusiveArea);

        String userPrompt = String.format("""
                %s
                
                ---
                [참고: 웹 검색 결과]
                다음은 위 정보를 찾기 위해 수행한 Tavily 검색 결과입니다. 
                아래 검색 결과를 바탕으로 위 지침에 따라 분석을 수행해주세요.
                
                %s
                """, instructionPrompt, searchResult);

        //log.info("AI 아파트 가격 조회 시작: {} - {}, {}㎡", address, apartmentName, exclusiveArea);

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)));

            ChatResponse response = chatModel.call(prompt);

            String aiResponse = response.getResult().getOutput().getText();
            log.info("AI 원본 응답: {}", aiResponse);

            AiApartmentPriceResponse priceData = parseAiResponse(aiResponse);

            if (priceData.hasNoData()) {
                log.warn("AI가 거래 데이터 없음을 보고함");
                return MarketPrice.builder().marketPrice(0L).info("데이터 없음").build();
            }

            // 비정상적으로 높은 가격 검증 및 자동 수정 (대형 평형 가격 혼입 또는 단위 오류)
            double exclusiveAreaDouble = Double.parseDouble(exclusiveArea);
            long pricePerSqm = priceData.averagePrice() / (long) exclusiveAreaDouble;

            // 1차 검증: ㎡당 가격이 1억 이상이면 단위 오류 (10배 잘못 계산)
            if (pricePerSqm > 100_000_000) {
                long correctedPrice = priceData.averagePrice() / 10;
                long correctedPricePerSqm = correctedPrice / (long) exclusiveAreaDouble;

                log.warn("단위 오류 감지 및 자동 수정! 원본: {}원 → 수정: {}원 (㎡당 {}원 → {}원)",
                        priceData.averagePrice(), correctedPrice, pricePerSqm, correctedPricePerSqm);

                // 수정된 가격이 합리적인지 재검증 (㎡당 1천만원~8천만원 범위)
                if (correctedPricePerSqm >= 10_000_000 && correctedPricePerSqm <= 80_000_000) {
                    String info = String.format("APARTMENT - AI(단위수정): %s %s㎡ (평균 %d건, 신뢰도: %s, 출처: %s)",
                            apartmentName, exclusiveArea, priceData.transactionCount(),
                            priceData.confidence(), priceData.dataSource());

                    log.info("단위 수정 후 정상 범위 진입: {}원 - {}", correctedPrice, info);

                    return MarketPrice.builder()
                            .marketPrice(correctedPrice)
                            .info(info)
                            .build();
                }
            }

            // 2차 검증: ㎡당 가격이 8천만원 이상이면 다른 평형 혼입 의심
            if (pricePerSqm > 80_000_000) {
                log.warn("비정상적으로 높은 가격 감지! averagePrice: {}원 (㎡당 {}원) - 다른 평형 가격 혼입 의심",
                        priceData.averagePrice(), pricePerSqm);
                log.warn("   요청 면적: {}㎡, 가격 범위: {}~{}",
                        exclusiveArea, priceData.priceRange().min(), priceData.priceRange().max());
                return MarketPrice.builder()
                        .marketPrice(0L)
                        .info("가격 검증 실패 (다른 평형 혼입 의심)")
                        .build();
            }

            // 3차 검증: ㎡당 가격이 1천만원 미만이면 너무 낮음 (전세가 혼입 의심)
            if (pricePerSqm < 10_000_000 && priceData.averagePrice() > 0) {
                log.warn("비정상적으로 낮은 가격 감지! averagePrice: {}원 (㎡당 {}원) - 전세가 혼입 의심",
                        priceData.averagePrice(), pricePerSqm);
                return MarketPrice.builder()
                        .marketPrice(0L)
                        .info("가격 검증 실패 (전세가 혼입 의심)")
                        .build();
            }

            if (!priceData.isReliable()) {
                log.warn("AI 응답 신뢰도 낮음: {}", priceData.confidence());
                if (priceData.averagePrice() > 0) {
                     String info = String.format("APARTMENT - AI(신뢰도낮음): %s %s㎡ (평균 %d건, 출처: %s)",
                            apartmentName, exclusiveArea, priceData.transactionCount(), priceData.dataSource());
                     return MarketPrice.builder()
                            .marketPrice(priceData.averagePrice())
                            .info(info)
                            .build();
                }
                return MarketPrice.builder().marketPrice(0L).info("신뢰도 낮음").build();
            }

            String info = String.format("APARTMENT - AI: %s %s㎡ (평균 %d건, 신뢰도: %s, 출처: %s)",
                    apartmentName, exclusiveArea, priceData.transactionCount(), priceData.confidence(), priceData.dataSource());

            log.info("AI 아파트 가격 조회 완료: {}원 (가격범위: {}~{}) - {}",
                    priceData.averagePrice(),
                    priceData.priceRange().min(),
                    priceData.priceRange().max(),
                    info);

            return MarketPrice.builder()
                    .marketPrice(priceData.averagePrice())
                    .info(info)
                    .build();

        } catch (Exception e) {
            log.error("AI 분석 중 오류 발생", e);
            return MarketPrice.builder().marketPrice(0L).info("AI 오류").build();
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