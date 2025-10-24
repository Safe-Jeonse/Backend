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

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickCheckService {

    private final ChatModel chatModel;
    private final PromptManger promptManger;
    private final BuildingLedgerAnalysisService buildingLedgerAnalysisService;

    public String quickCheck(PromptDto promptDto) {
        log.info("QuickCheck 분석 시작");

        String systemPrompt = promptManger.getSystemPrompt(promptDto);
        String userPrompt = promptManger.getQuickCheckPrompt(promptDto);
        buildingLedgerAnalysisService.result(promptDto.getAddress());
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
