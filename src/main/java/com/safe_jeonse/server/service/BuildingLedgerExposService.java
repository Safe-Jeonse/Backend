package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.BuildingLedgerExposResponse;
import com.safe_jeonse.server.dto.response.BuildingLedgerExposRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.safe_jeonse.server.util.LnbrUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class BuildingLedgerExposService {

    @Value("${spring.open-data.open-data-key}")
    private String apiKey;

    @Value("${spring.open-data.BL-expos-url}")
    private String url;

    private final RestTemplate restTemplate;

    public BuildingLedgerExposResponse getBLExpos(String lnbrMnnm, String lnbrSlno, String sigunguCd, String bjdongCd, String dong, String ho) {
        String bun = LnbrUtils.padTo4(lnbrMnnm);
        String ji = LnbrUtils.padTo4(lnbrSlno);

        BuildingLedgerExposResponse resp = requestExpos(bun, ji, sigunguCd, bjdongCd, dong, ho);
        if (!isEmpty(resp)) return resp;

        String hoNoSuffix = stripHoSuffix(ho);
        if (!equalsNullable(ho, hoNoSuffix)) {
            resp = requestExpos(bun, ji, sigunguCd, bjdongCd, dong, hoNoSuffix);
            if (!isEmpty(resp)) return resp;
        }

        String dongNumeric = toNumericOrNull(dong);
        if (!equalsNullable(dong, dongNumeric)) {
            String hoCandidate = !equalsNullable(ho, hoNoSuffix) ? hoNoSuffix : ho;
            resp = requestExpos(bun, ji, sigunguCd, bjdongCd, dongNumeric, hoCandidate);
            if (!isEmpty(resp)) return resp;
        }

        String hoNumeric = toNumericOrNull(ho);
        if (!equalsNullable(dong, dongNumeric) || !equalsNullable(ho, hoNumeric)) {
            resp = requestExpos(bun, ji, sigunguCd, bjdongCd, dongNumeric, hoNumeric);
        }
        return resp;
    }

    private BuildingLedgerExposResponse requestExpos(String bun, String ji, String sigunguCd, String bjdongCd, String dong, String ho) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("serviceKey", apiKey);
        params.add("sigunguCd", sigunguCd);
        params.add("bjdongCd", bjdongCd);
        params.add("bun", bun);
        params.add("ji", ji);
        if (dong != null && !dong.isBlank()) params.add("dongNm", dong);
        if (ho != null && !ho.isBlank()) params.add("hoNm", ho);
        params.add("_type", "json");

        String uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParams(params)
                .build()
                .toUriString();

        ResponseEntity<BuildingLedgerExposRoot> response = restTemplate.getForEntity(uri, BuildingLedgerExposRoot.class);
        return response.getBody() != null ? response.getBody().getResponse() : null;
    }

    private boolean isEmpty(BuildingLedgerExposResponse mapped) {
        if (mapped == null || mapped.getBody() == null || mapped.getBody().getItems() == null) return true;
        var list = mapped.getBody().getItems().getItem();
        if (list == null || list.isEmpty()) return true;
        int total = mapped.getBody().getTotalCount();
        return total == 0;
    }

    private static String stripHoSuffix(String ho) {
        if (ho == null) return null;
        String trimmed = ho.trim();
        if (trimmed.isEmpty()) return trimmed;
        return trimmed.replaceFirst("\\s*호$", "");
    }

    private static String toNumericOrNull(String s) {
        if (s == null) return null;
        String onlyDigits = s.replaceAll("\\D", "").trim();
        return onlyDigits.isEmpty() ? null : onlyDigits;
    }

    private static boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
