package com.safe_jeonse.server.dto;

import com.safe_jeonse.server.dto.response.OcrResultDto;
import lombok.Getter;

@Getter
public class PromptDto {

    private String address;

    private Long leaseDeposit;

    private String landlord;

    private OcrResultDto ocrResultDto;
}
