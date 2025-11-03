package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.JusoApiResponse;
import com.safe_jeonse.server.dto.response.JusoTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
/*
    시군구, 법정동 코드를 조회하는 비지니스 로직
    추후 토큰 발급은 운영 환경에서는 @Scheduled로 4시간 마다 갱신하도록 수정
 */
public class JusoApiClient {

    private final JusoApiTokenProvider jusoApiTokenProvider;
    private final RestTemplate restTemplate;

    @Value("${spring.open-data.SGIS-code-url}")
    private String url;

    public JusoApiResponse getJusoCode(String address) {

        JusoTokenResponse jusoTokenResponse = jusoApiTokenProvider.getToken();
        String token = jusoTokenResponse.result().accessToken();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("accessToken", token);
        params.add("address", address);

        String uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<JusoApiResponse> response = restTemplate.getForEntity(uri, JusoApiResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("SGIS 코드 요청 실패: " + response.getStatusCode());
        }
    }


}
