package com.safe_jeonse.server.dto;

import com.safe_jeonse.server.dto.response.ParseResultDto;
import lombok.Getter;

@Getter
public class PromptDto {

    private String address;

    private Long leaseDeposit;

    private String landlord;

    private ParseResultDto parseResultDto;
}
