package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.AddressResponse;
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
@Slf4j
@RequiredArgsConstructor
public class JusoApiTokenProvider {

    private final RestTemplate restTemplate;

    @Value("${spring.open-data.SGIS-key}")
    private String consumerKey;

    @Value("${spring.open-data.SGIS-secret}")
    private String consumerSecret;

    @Value("${spring.open-data.SGIS-token-url}")
    private String url;

    public JusoTokenResponse getToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("consumer_key", consumerKey);
        params.add("consumer_secret", consumerSecret);

        String uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<JusoTokenResponse> response = restTemplate.getForEntity(uri, JusoTokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("SGIS 토큰 요청 실패: " + response.getStatusCode());
        }

    }
}
