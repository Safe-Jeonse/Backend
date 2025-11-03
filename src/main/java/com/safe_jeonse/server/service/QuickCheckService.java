package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.PromptDto;
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
public class QuickCheckService {

    private final ChatModel chatModel;
    private final PromptManger promptManger;
    private final BuildingLedgerAnalysisService buildingLedgerAnalysisService;

    public String quickCheck(PromptDto promptDto) {

        // buildingLedgerResult를 비동기로 요청하고 짧은 타임아웃(5초)으로 기다림.
        CompletableFuture<String> future = buildingLedgerAnalysisService.buildingLedgerResultAsync(promptDto.getAddress());

        String builderLedgerResult;
        try {
            builderLedgerResult = future.get(5000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("buildingLedgerResult 호출이 지연/실패하여 폴백값 사용: {}", e.toString());
            builderLedgerResult = "건축물 대장 분석 결과를 불러오는 데 실패했습니다.";
            future.cancel(true);
        }


        PromptDto dtoWithLedger = PromptDto.builder()
                .address(promptDto.getAddress())
                .leaseDeposit(promptDto.getLeaseDeposit())
                .landlord(promptDto.getLandlord())
                .parseResultDto(promptDto.getParseResultDto())
                .buildingLedgerResult(builderLedgerResult)
                .build();

        String systemPrompt = promptManger.getSystemPrompt(dtoWithLedger);
        String userPrompt = promptManger.getQuickCheckPrompt(dtoWithLedger);
        log.debug("생성된 시스템 프롬프트: {}", systemPrompt);
        log.debug("생성된 사용자 프롬프트: {}", userPrompt);

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));

            ChatResponse response = chatModel.call(prompt);

            return response.getResult().getOutput().toString();
        } catch (Exception e) {
            log.error("AI API 호출 중 오류 발생", e);
            throw new AiApiException("AI 분석 중 오류가 발생했습니다.", e);
        }
    }
}
