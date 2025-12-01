package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.BuildingAddressInfo;
import com.safe_jeonse.server.dto.BuildingLedgerAnalysisResult;
import com.safe_jeonse.server.dto.PromptDto;
import com.safe_jeonse.server.dto.response.MarketPrice;
import com.safe_jeonse.server.exception.AiApiException;
import com.safe_jeonse.server.prompt.PromptManger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepCheckService {

    private final ChatModel chatModel;
    private final PromptManger promptManger;
    private final BuildingLedgerAnalysisService buildingLedgerAnalysisService;
    private final marketPriceService marketPriceService;

    public String deepCheck(PromptDto promptDto) {

        BuildingAddressInfo addressInfo = buildingLedgerAnalysisService.getBuildingAddressInfo(promptDto.getAddress());

        // buildingLedgerResult를 비동기로 요청하고 짧은 타임아웃(5초)으로 기다림.
        CompletableFuture<BuildingLedgerAnalysisResult> future = buildingLedgerAnalysisService
                .buildingLedgerResultAsync(addressInfo);

        BuildingLedgerAnalysisResult analysisResult;
        try {
            analysisResult = future.get(5000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("buildingLedgerResult 호출이 지연/실패하여 폴백값 사용(DeepCheck): {}", e.toString());
            analysisResult = BuildingLedgerAnalysisResult.builder()
                    .analysisMessage("건축물 대장 분석 결과를 불러오는 데 실패했습니다.")
                    .hhldCnt(0)
                    .fmlyCnt(0)
                    .build();
            future.cancel(true);
        }

        MarketPrice marketPrice = marketPriceService.getMarketPrice(addressInfo, promptDto.getIsApartment(),
                analysisResult.hhldCnt(), analysisResult.fmlyCnt());

        // PromptDto에 빌딩대장 결과를 주입한 새 DTO를 생성
        PromptDto dtoWithLedger = PromptDto.builder()
                .address(promptDto.getAddress())
                .leaseDeposit(promptDto.getLeaseDeposit())
                .landlord(promptDto.getLandlord())
                .localDateTime(promptDto.getLocalDateTime())
                .parseResultDto(promptDto.getParseResultDto())
                .buildingLedgerResult(analysisResult.analysisMessage())
                .marketPrice(marketPrice.marketPrice())
                .info(marketPrice.info())
                .userMarketPrice(promptDto.getUserMarketPrice())
                .build();

        String systemPrompt = promptManger.getSystemPrompt(dtoWithLedger);
        String userPrompt = promptManger.getDeepCheckPrompt(dtoWithLedger);
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)));

            ChatResponse response = chatModel.call(prompt);

            return response.getResult().getOutput().toString();
        } catch (Exception e) {
            log.error("AI API 호출 중 오류 발생", e);
            throw new AiApiException("AI 분석 중 오류가 발생했습니다.", e);
        }
    }
}
