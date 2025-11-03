package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.BuildingLedgerTitleResponse;
import com.safe_jeonse.server.dto.response.BuildingLedgerTitleRoot;
import com.safe_jeonse.server.util.LnbrUtils;
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
public class BuildingLedgerTitleService {

    @Value("${spring.open-data.open-data-key}")
    private String apiKey;

    @Value("${spring.open-data.BL-title-url}")
    private String url;

    private final RestTemplate restTemplate;

    public BuildingLedgerTitleResponse getBLTitle(String lnbrMnnm, String lnbrSlno, String sigunguCd, String bjdongCd) {

        String bun = LnbrUtils.padTo4(lnbrMnnm);
        String ji = LnbrUtils.padTo4(lnbrSlno);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("serviceKey", apiKey);
        params.add("sigunguCd", sigunguCd);
        params.add("bjdongCd", bjdongCd);
        params.add("numOfRows", "10");
        params.add("pageNo", "1");
        params.add("bun", bun);
        params.add("ji", ji);
        params.add("_type", "json");

        String uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<BuildingLedgerTitleRoot> response = restTemplate.getForEntity(uri, BuildingLedgerTitleRoot.class);
        BuildingLedgerTitleResponse mapped = response.getBody() != null ? response.getBody().getResponse() : null;

        return mapped;
    }
}
