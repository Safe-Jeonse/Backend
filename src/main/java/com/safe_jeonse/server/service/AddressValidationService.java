package com.safe_jeonse.server.service;

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
            AddressResponse response = searchAddress(address);
            // 검색 결과가 있는지 확인
            if (response.getResults().getTotalCount() > 0 && !response.getResults().getJuso().isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("주소 검증 중 오류 발생: {}", e.getMessage(), e);
            return false;
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

        // 요청 보내기
        ResponseEntity<AddressResponse> response = restTemplate.getForEntity(uri, AddressResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("주소 API 요청 실패: " + response.getStatusCode());
        }
    }

}