package com.safe_jeonse.server.dto;

import com.safe_jeonse.server.dto.request.ReportRequest;
import com.safe_jeonse.server.dto.response.ParseResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PromptDto {

    private String address;

    private Long leaseDeposit;

    private String landlord;

    private ParseResultDto parseResultDto;

    // ReportRequest로부터 PromptDto 생성 (파일 없는 경우)
    public static PromptDto fromRequest(ReportRequest request) {
        return PromptDto.builder()
                .address(request.getAddress())
                .leaseDeposit(request.getLeaseDeposit())
                .landlord(request.getLandlord())
                .parseResultDto(null)
                .build();
    }

    // ReportRequest와 ParseResultDto로부터 PromptDto 생성 (파일 있는 경우)
    public static PromptDto fromRequestWithParse(ReportRequest request, ParseResultDto parseResultDto) {
        return PromptDto.builder()
                .address(request.getAddress())
                .leaseDeposit(request.getLeaseDeposit())
                .landlord(request.getLandlord())
                .parseResultDto(parseResultDto)
                .build();
    }
}
