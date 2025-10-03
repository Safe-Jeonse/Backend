package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.request.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final QuickCheckService quickCheckService;
    private final DeepCheckService deepCheckService;

    public String generateReport(ReportRequest reportRequest) {

        // 사용자의 등기부등본 업로드 유무에 따른 분기
        if(reportRequest.getRegistryFile().isEmpty()) {
            return quickCheckService.quickCheck();
        }
        else {
            return deepCheckService.deepCheck();
        }
    }

}
