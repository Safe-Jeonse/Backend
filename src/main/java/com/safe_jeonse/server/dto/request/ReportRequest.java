package com.safe_jeonse.server.dto.request;

import com.safe_jeonse.server.validation.annotation.ValidAddress;
import com.safe_jeonse.server.validation.annotation.ValidPdf;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReportRequest {

    public static final long MAX_LEASE_DEPOSIT = 10_000_000_000L;
    public static final String KOREAN_NAME_REGEX_STRICT = "^[가-힣]{2,4}$";

    @NotBlank(message = "주소를 입력해 주세요.")
    @ValidAddress
    private String address;

    @Max(value = MAX_LEASE_DEPOSIT, message = "임대 보증금은 100억 이하로 입력해주세요.")
    @NotNull(message = "전세 보증금을 입력해 주세요.")
    private Long leaseDeposit;

    @Pattern(regexp = KOREAN_NAME_REGEX_STRICT, message = "이름을 2 ~ 4 글자의 한글로 입력해주세요.")
    private String landlord;

    @Max(value = MAX_LEASE_DEPOSIT, message = "실 거래가는 100억 이하로 입력해주세요.")
    private Long userMarketPrice;

    @NotNull(message = "아파트/다세대 여부를 확인할 수 없습니다.")
    private String isApartment;

    @ValidPdf
    private MultipartFile registryFile;

}