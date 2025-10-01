package com.safe_jeonse.server.dto.request;


import com.safe_jeonse.server.validation.annotation.ValidPdf;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReportRequest {

    public static final long MAX_LEASE_DEPOSIT = 10_000_000_000L;
    public static final String KOREAN_NAME_REGEX_STRICT = "^[가-힣]{2,4}$";

    @NotBlank
    private String address;

    @Max(MAX_LEASE_DEPOSIT)
    @NotNull
    private Long leaseDeposit;

    @Pattern(regexp = KOREAN_NAME_REGEX_STRICT)
    private String landlord;

    @ValidPdf
    private MultipartFile registryFile;

}