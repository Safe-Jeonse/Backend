package com.safe_jeonse.server.dto;

import lombok.Builder;

@Builder
public record BuildingLedgerAnalysisResult(
        String analysisMessage,
        int hhldCnt,
        int fmlyCnt) {
}
