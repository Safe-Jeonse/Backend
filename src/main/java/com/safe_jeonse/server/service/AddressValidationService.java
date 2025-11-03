package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.AddressValidationResult;
import com.safe_jeonse.server.dto.response.AddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressValidationService {

    @Value("${spring.open-data.address-api-key}")
    private String confmKey;

    @Value("${spring.open-data.address-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public boolean validateAddress(String address) {
        try {
            return validateAndExtractLnbr(address).isValid();
        } catch (Exception e) {
            log.error("주소 검증 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * validateAddress의 확장 버전으로, 유효성 여부와 함께 lnbrMnnm을 추출해 반환합니다.
     * - 기존 validateAddress는 그대로 유지되어 하위 호환성을 보장합니다.
     */
    public AddressValidationResult validateAndExtractLnbr(String address) {
        try {
            AddressResponse response = searchAddress(address);
            if (response == null) return new AddressValidationResult(false, null, null);

            var results = response.getResults();
            if (results == null) return new AddressValidationResult(false, null, null);

            List<AddressResponse.Juso> jusoList = results.getJuso();
            if (jusoList == null || jusoList.isEmpty()) return new AddressValidationResult(false, null, null);

            AddressResponse.Juso top = jusoList.get(0);
            if (top == null) return new AddressValidationResult(false, null, null);

            String lnbr = top.getLnbrMnnm();
            String lnbrSlno = top.getLnbrSlno();

            if (lnbr != null) lnbr = lnbr.trim();
            if (lnbr != null && (lnbr.isEmpty() || "null".equalsIgnoreCase(lnbr))) lnbr = null;

            if (lnbrSlno != null) lnbrSlno = lnbrSlno.trim();
            if (lnbrSlno != null && (lnbrSlno.isEmpty() || "null".equalsIgnoreCase(lnbrSlno))) lnbrSlno = null;

            boolean valid = results.getTotalCount() > 0 && !jusoList.isEmpty();

            return new AddressValidationResult(valid, lnbr, lnbrSlno);
        } catch (Exception e) {
            log.error("validateAndExtractLnbr 처리 중 오류: {}", e.getMessage(), e);
            return new AddressValidationResult(false, null, null);
        }
    }

    /**
     * 주소 검색을 통해 lnbrMnnm(지번 본번)을 안전하게 추출해 반환합니다.
     * - 결과가 없거나 값이 비어있거나 문자열 "null"인 경우 Optional.empty() 반환
     * - 여러 결과가 있는 경우 첫 번째 결과를 사용(필요 시 별도 로직으로 변경)
     */
    public Optional<String> extractLnbrMnnm(String address) {
        try {
            AddressResponse response = searchAddress(address);
            if (response == null) {
                return Optional.empty();
            }

            var results = response.getResults();
            if (results == null) return Optional.empty();

            List<AddressResponse.Juso> jusoList = results.getJuso();
            if (jusoList == null || jusoList.isEmpty()) return Optional.empty();

            AddressResponse.Juso top = jusoList.get(0);
            if (top == null) return Optional.empty();

            String lnbr = top.getLnbrMnnm();
            // API가 "null" 문자열을 반환하는 경우가 있어 이를 정상화
            if (lnbr == null) return Optional.empty();
            lnbr = lnbr.trim();
            if (lnbr.isEmpty() || "null".equalsIgnoreCase(lnbr)) return Optional.empty();

            return Optional.of(lnbr);
        } catch (Exception e) {
            log.warn("lnbrMnnm 추출 중 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public AddressResponse searchAddress(String address) {
        // API 요청 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("confmKey", confmKey);
        params.add("currentPage", "1");
        params.add("countPerPage", "10");
        params.add("keyword", address);
        params.add("resultType", "json");

        // 선택적 파라미터
        params.add("hstryYn", "N");
        params.add("firstSort", "none");
        params.add("addInfoYn", "N");

        // URI 생성
        String uri = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<AddressResponse> response = restTemplate.getForEntity(uri, AddressResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("주소 API 요청 실패: " + response.getStatusCode());
        }
    }

}