package com.safe_jeonse.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safe_jeonse.server.dto.PromptDto;
import com.safe_jeonse.server.dto.request.ReportRequest;
import com.safe_jeonse.server.dto.response.ParseResultDto;
import com.safe_jeonse.server.dto.response.ReportResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final QuickCheckService quickCheckService;
    private final DeepCheckService deepCheckService;
    private final PdfParseService pdfParseService;
    private final ObjectMapper objectMapper;

    public ReportResponseDto generateReport(ReportRequest reportRequest) {

        String jsonResponse;
        // 등기부등본 파일이 없거나 비어있는 경우 - QuickCheck
        if(reportRequest.getRegistryFile() == null || reportRequest.getRegistryFile().isEmpty()) {
            PromptDto promptDto = PromptDto.fromRequest(reportRequest);
            jsonResponse = quickCheckService.quickCheck(promptDto);
        }
        // 등기부등본 파일이 있는 경우 - DeepCheck
        else {
            ParseResultDto parseResultDto = pdfParseService.parsePdf(reportRequest.getRegistryFile());
            PromptDto promptDto = PromptDto.fromRequestWithParse(reportRequest, parseResultDto);
            jsonResponse = deepCheckService.deepCheck(promptDto);
        }

        try {
            return objectMapper.readValue(jsonResponse, ReportResponseDto.class);
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", jsonResponse, e);
            throw new RuntimeException("리포트 생성 중 오류가 발생했습니다.", e);
        }
    }

}
