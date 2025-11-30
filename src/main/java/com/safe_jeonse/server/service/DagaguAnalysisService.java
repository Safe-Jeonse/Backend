package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.BuildingAddressInfo;
import com.safe_jeonse.server.dto.response.DagaguPriceResponse;
import com.safe_jeonse.server.dto.response.MarketPrice;
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
public class DagaguAnalysisService {

    @Value("${spring.open-data.vworld-url}")
    private String url;

    @Value("${spring.open-data.vworld-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public MarketPrice getPublicPrice(BuildingAddressInfo addressInfo, int hhldCnt, int fmlyCnt) {

        String pnu = addressInfo.bjdongCd() + addressInfo.sigunguCd() + "1" + addressInfo.lnbrMnnm() + addressInfo.lnbrSlno();
        long gaguCount = hhldCnt + fmlyCnt;

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("pnu", pnu);
        params.add("key", apiKey);
        params.add("format", "json");
        params.add("domain", "http://localhost");

        String uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<DagaguPriceResponse> response = restTemplate.getForEntity(uri, DagaguPriceResponse.class);

        Long housePc = response.getBody()
                .getIndvdHousingPrices()
                .getField()
                .get(0)
                .getHousePriceAsLong();

        MarketPrice dagagu = MarketPrice.builder()
                .marketPrice(housePc / gaguCount)
                .info("DAGAGU")
                .build();

        return dagagu;
    }
}
