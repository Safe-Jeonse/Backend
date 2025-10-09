package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.PromptDto;
import com.safe_jeonse.server.exception.AiApiException;
import com.safe_jeonse.server.prompt.PromptManger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickCheckService {

    private final ChatModel chatModel;
    private final PromptManger promptManger;

    public String quickCheck(PromptDto promptDto) {
        log.info("QuickCheck 분석 시작");

        String userPrompt = promptManger.getQuickCheckPrompt(promptDto);
        log.debug("생성된 프롬프트: {}", userPrompt);

        try {
            Prompt prompt = new Prompt(userPrompt);
            ChatResponse response = chatModel.call(prompt);

            // String result = response.getResult().getOutput().getContent();

            // return result;
            return "!";
        } catch (Exception e) {
            log.error("AI API 호출 중 오류 발생", e);
            throw new AiApiException("AI 분석 중 오류가 발생했습니다.", e);
        }
    }
}
