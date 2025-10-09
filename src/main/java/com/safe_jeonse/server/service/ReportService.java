package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.PromptDto;
import com.safe_jeonse.server.dto.request.ReportRequest;
import com.safe_jeonse.server.dto.response.ParseResultDto;
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

    public String generateReport(ReportRequest reportRequest) {

        // 등기부등본 파일이 없거나 비어있는 경우 - QuickCheck
        if(reportRequest.getRegistryFile() == null || reportRequest.getRegistryFile().isEmpty()) {
            PromptDto promptDto = PromptDto.fromRequest(reportRequest);
            return quickCheckService.quickCheck(promptDto);
        }
        // 등기부등본 파일이 있는 경우 - DeepCheck
        else {
            ParseResultDto parseResultDto = pdfParseService.parsePdf(reportRequest.getRegistryFile());
            PromptDto promptDto = PromptDto.fromRequestWithParse(reportRequest, parseResultDto);
            return deepCheckService.deepCheck(promptDto);
        }
    }

}
