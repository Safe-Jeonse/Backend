package com.safe_jeonse.server.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ReportResponse(
        UUID reportId,
        Boolean analysisType,
        String overallRiskLevel,
        PropertyInfo propertyInfo,
        List<RiskItem> detailedRiskAnalysis,
        String aiGeneralComment,
        List<ChecklistItem> humanChecklist
) {
    public record PropertyInfo(
            String address,
            Long marketPrice,
            Long leaseDeposit,
            Double riskRatio
    ) {}

    public record RiskItem(
            String category,
            String riskLevel,
            String comment
    ) {}

    public record ChecklistItem(
            String title,
            String description,
            Boolean isEssential
    ) {}
}
