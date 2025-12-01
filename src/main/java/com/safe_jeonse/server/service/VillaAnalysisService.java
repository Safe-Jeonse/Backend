package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.BuildingAddressInfo;
import com.safe_jeonse.server.dto.response.ApartPriceResponse;
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

/*
    빌라(다세대,연립) 및 오피스텔 공시가 조회 로직
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VillaAnalysisService {

    @Value("${spring.open-data.vworld-villa-url}")
    private String villaApiUrl;

    @Value("${spring.open-data.vworld-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public MarketPrice getPublicPrice(BuildingAddressInfo addressInfo) {

        if(isOfficeTell(addressInfo.address())) {

        }
        String pnu = addressInfo.bjdongCd() + addressInfo.sigunguCd() + "1" + addressInfo.lnbrMnnm() + addressInfo.lnbrSlno();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("pnu", pnu);
        params.add("key", apiKey);
        params.add("format", "json");
        params.add("domain", "http://localhost");

        String uri = UriComponentsBuilder
                .fromHttpUrl(villaApiUrl)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<ApartPriceResponse> response = restTemplate.getForEntity(uri, ApartPriceResponse.class);

        Long housePc = response.getBody()
                .getApartHousingPrices()
                .getField()
                .get(0)
                .getPblntfPcAsLong();

        return MarketPrice.builder()
                .info("VILLA")
                .marketPrice(housePc)
                .build();
    }

    public boolean isOfficeTell(String address) {
        return false;
    }

}
