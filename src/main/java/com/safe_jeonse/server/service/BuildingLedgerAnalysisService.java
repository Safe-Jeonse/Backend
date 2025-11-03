package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.BuildingLedgerExposResponse;
import com.safe_jeonse.server.dto.response.BuildingLedgerTitleResponse;
import com.safe_jeonse.server.dto.response.JusoApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    건축물 대장 표제부, 전유부 조회를 통한 분석 비지니스 로직
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BuildingLedgerAnalysisService {

    private final JusoApiClient jusoApiClient;
    private final AddressContext addressContext;
    private final BuildingLedgerTitleService buildingLedgerTitleService;
    private final BuildingLedgerExposService buildingLedgerExposService;

    private static final Pattern DONG_PATTERN = Pattern.compile("(\\d+)\\s*동");
    private static final Pattern HO_PATTERN = Pattern.compile("(\\d+)\\s*호");

    private static String extractDong(String address) {
        if (address == null) return null;
        Matcher m = DONG_PATTERN.matcher(address);
        if (m.find()) return m.group(1) + "동";
        return null;
    }

    private static String extractHo(String address) {
        if (address == null) return null;
        Matcher m = HO_PATTERN.matcher(address);
        if (m.find()) return m.group(1) + "호";
        return null;
    }

    String buildingLedgerResult(String address){
        String lnbrMnnm = addressContext.getLnbrMnnm().orElse(null);
        String lnbrSlno = addressContext.getLnbrSlno().orElse(null);
        return buildingLedgerResultInternal(address, lnbrMnnm, lnbrSlno);
    }

    public CompletableFuture<String> buildingLedgerResultAsync(String address) {
        // RequestScope는 비동기 스레드에서 활성화되지 않으므로 여기서 값을 캡처
        String capturedLnbr = addressContext.getLnbrMnnm().orElse(null);
        String capturedLnbrSlno = addressContext.getLnbrSlno().orElse(null);
        return CompletableFuture.supplyAsync(() -> buildingLedgerResultInternal(address, capturedLnbr, capturedLnbrSlno));
    }

    // lnbrMnnm = 지번 lnbrSlno = 부번
    private String buildingLedgerResultInternal(String address, String lnbrMnnm, String lnbrSlno) {

        JusoApiResponse response = jusoApiClient.getJusoCode(address);
        String legCd = response.result().resultdata().get(0).getLegCd();
        log.info(legCd);
        log.info(address);

        String sigunguCd = legCd.substring(0, 5);
        String bjdongCd = legCd.substring(5, 10);
        String dong = extractDong(address);
        String ho = extractHo(address);

        String builderLedgerResult = "없음";

        BuildingLedgerTitleResponse titleResult = buildingLedgerTitleService.getBLTitle(lnbrMnnm, lnbrSlno, sigunguCd, bjdongCd);

        if (lnbrMnnm != null) {
            log.info("lnbrMnnm을 사용하여 건축물 대장 조회를 수행합니다: {}", lnbrMnnm);
        } else {
            log.info("lnbrMnnm이 없습니다.");
        }

        return builderLedgerResult;
    }

}
