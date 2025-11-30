package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.BuildingAddressInfo;
import com.safe_jeonse.server.dto.BuildingLedgerAnalysisResult;
import com.safe_jeonse.server.dto.response.BuildingLedgerExposResponse;
import com.safe_jeonse.server.dto.response.BuildingLedgerTitleResponse;
import com.safe_jeonse.server.dto.response.JusoApiResponse;
import com.safe_jeonse.server.util.hoParseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private final hoParseUtils hoParseUtils;

    private static final Pattern DONG_PATTERN = Pattern.compile("(\\d+)\\s*동");
    private static final Pattern HO_PATTERN = Pattern.compile("(\\d+)\\s*호");

    private static String extractDong(String address) {
        if (address == null)
            return null;
        Matcher m = DONG_PATTERN.matcher(address);
        if (m.find())
            return m.group(1) + "동";
        return null;
    }

    private static String extractHo(String address) {
        if (address == null)
            return null;
        Matcher m = HO_PATTERN.matcher(address);
        if (m.find())
            return m.group(1) + "호";
        return null;
    }

    private static String onlyDigitsOrNull(String s) {
        if (s == null)
            return null;
        String d = s.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    private static List<BuildingLedgerExposResponse.Item> filterUnitsByDongHo(BuildingLedgerExposResponse exposResponse,
            String dong, String ho) {
        List<BuildingLedgerExposResponse.Item> matches = new ArrayList<>();
        if (exposResponse == null || exposResponse.getBody() == null || exposResponse.getBody().getItems() == null)
            return matches;
        List<BuildingLedgerExposResponse.Item> items = exposResponse.getBody().getItems().getItem();
        if (items == null)
            return matches;
        String wantDong = onlyDigitsOrNull(dong);
        String wantHo = onlyDigitsOrNull(ho);
        for (BuildingLedgerExposResponse.Item it : items) {
            if (it == null)
                continue;
            String itemDong = onlyDigitsOrNull(it.getDongNm());
            String itemHo = onlyDigitsOrNull(it.getHoNm());
            boolean dongOk = (wantDong == null) || (wantDong.equals(itemDong));
            boolean hoOk = (wantHo == null) || (wantHo.equals(itemHo));
            if (dongOk && hoOk) {
                matches.add(it);
            }
        }
        return matches;
    }

    private static List<BuildingLedgerTitleResponse.Item> filterTitleByDong(BuildingLedgerTitleResponse titleResponse,
            String dong) {
        List<BuildingLedgerTitleResponse.Item> matches = new ArrayList<>();
        if (titleResponse == null || titleResponse.getBody() == null || titleResponse.getBody().getItems() == null)
            return matches;
        List<BuildingLedgerTitleResponse.Item> items = titleResponse.getBody().getItems().getItem();
        if (items == null)
            return matches;
        String wantDong = onlyDigitsOrNull(dong);
        for (BuildingLedgerTitleResponse.Item it : items) {
            if (it == null)
                continue;
            String itemDong = onlyDigitsOrNull(it.getDongNm());
            if (wantDong == null || wantDong.equals(itemDong)) {
                matches.add(it);
            }
        }
        return matches;
    }

    public BuildingAddressInfo getBuildingAddressInfo(String address) {
        String lnbrMnnm = addressContext.getLnbrMnnm().orElse(null);
        String lnbrSlno = addressContext.getLnbrSlno().orElse(null);

        JusoApiResponse response = jusoApiClient.getJusoCode(address);
        String legCd = response.result().resultdata().get(0).getLegCd();

        String sigunguCd = legCd.substring(0, 5);
        String bjdongCd = legCd.substring(5, 10);
        String dong = extractDong(address);
        String ho = extractHo(address);

        return BuildingAddressInfo.builder()
                .address(address)
                .lnbrMnnm(lnbrMnnm)
                .lnbrSlno(lnbrSlno)
                .sigunguCd(sigunguCd)
                .bjdongCd(bjdongCd)
                .dong(dong)
                .ho(ho)
                .build();
    }

    public CompletableFuture<BuildingLedgerAnalysisResult> buildingLedgerResultAsync(BuildingAddressInfo addressInfo) {
        return CompletableFuture.supplyAsync(() -> buildingLedgerResultInternal(addressInfo));
    }

    // lnbrMnnm = 지번 lnbrSlno = 부번
    private BuildingLedgerAnalysisResult buildingLedgerResultInternal(BuildingAddressInfo info) {
        String lnbrMnnm = info.lnbrMnnm();
        String lnbrSlno = info.lnbrSlno();
        String sigunguCd = info.sigunguCd();
        String bjdongCd = info.bjdongCd();
        String dong = info.dong();
        String ho = info.ho();

        StringBuilder builderLedgerResult = new StringBuilder();

        BuildingLedgerTitleResponse titleResult = buildingLedgerTitleService.getBLTitle(lnbrMnnm, lnbrSlno, sigunguCd,
                bjdongCd);
        List<BuildingLedgerTitleResponse.Item> matchedTitles = filterTitleByDong(titleResult, dong);

        BuildingLedgerTitleResponse.Item titleItem = matchedTitles.get(0);
        int hhldCnt = parseCount(titleItem.getHhldCnt());
        int fmlyCnt = parseCount(titleItem.getFmlyCnt());

        builderLedgerResult.append(checkMainPurps(titleItem));
        Boolean isBuilding = checkBuildingType(titleItem);

        // 집합 건축물(아파트, 빌라)
        if (isBuilding) {
            BuildingLedgerExposResponse exposResponse = buildingLedgerExposService.getBLExpos(lnbrMnnm, lnbrSlno,
                    sigunguCd, bjdongCd, dong, ho);
            List<BuildingLedgerExposResponse.Item> matchedExpos = filterUnitsByDongHo(exposResponse, dong, ho);
            builderLedgerResult.append(checkExposPurps(matchedExpos.get(0)));
        }
        // 일반(다가구), 전유부 필요 x
        else {
            builderLedgerResult.append(checkRoomSplit());
            int parsedFloor = hoParseUtils.parseFloorFromHo(ho);
            builderLedgerResult.append(checkIllegalFloor(matchedTitles.get(0), parsedFloor));
        }
        return BuildingLedgerAnalysisResult.builder()
                .analysisMessage(builderLedgerResult.toString())
                .hhldCnt(hhldCnt)
                .fmlyCnt(fmlyCnt)
                .build();
    }

    private int parseCount(String cnt) {
        if (cnt == null || cnt.isEmpty())
            return 0;
        try {
            return Integer.parseInt(cnt);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * case1) 근생빌라 사기 방지 로직
     */
    private String checkMainPurps(BuildingLedgerTitleResponse.Item item) {

        String etcPurps = item.getEtcPurps();
        if (etcPurps.equals("근린생활시설") || etcPurps.equals("사무소") || etcPurps.equals("업무시설") || etcPurps.equals("판매시설")) {
            return "주용도: " + etcPurps + " ";
        }
        return "주용도: 이상없음 ";
    }

    /**
     * case2) 건물의 '대장 종류'를 확인
     */
    private boolean checkBuildingType(BuildingLedgerTitleResponse.Item item) {
        String type = item.getRegstrGbCdNm(); // "집합" 또는 "일반"

        if ("집합".equals(type)) {
            return true;
        }
        return false;
    }

    /**
     * case3) 전유부 조회후 처리 로직
     * 개별호수 용도 체크
     */
    private String checkExposPurps(BuildingLedgerExposResponse.Item item) {
        if (item == null) {
            return "주의 전유부 조회 불가 ";
        }
        String purps = item.getMainPurpsCdNm();
        if (purps.equals("근린생활시설") || purps.equals("사무소") || purps.equals("업무시설") || purps.equals("판매시설")
                || purps.equals("창고") || purps.equals("공장")) {
            return "전유부 주용도: " + purps + " ";
        } else {
            return "전유부 주용도: " + "이상없음 ";
        }
    }

    /**
     * case4) 일반건축물(다가구) 판독
     * '일반건축물'인데 '호수'로 계약한다는 사실 자체가 '방 쪼개기' 의심
     * '방 쪼개기' 경고를 반환
     */
    private String checkRoomSplit() {
        return "방쪼개기 의심(일반건축물) ";
    }

    /**
     * case5) 일반건축물 층수 초과 (불법 옥탑)를 확인
     */
    private String checkIllegalFloor(BuildingLedgerTitleResponse.Item titleItem, int parsedFloor) {
        try {

            int ledgerFloor = Integer.parseInt(titleItem.getGrndFlrCnt());

            // 사용자가 입력한 층수가 대장상 층수보다 크면
            if (parsedFloor > ledgerFloor) {
                return "층수초과(불법옥탑): 대장(" + ledgerFloor + "층), 요청(" + parsedFloor + "층) ";
            }

        } catch (NumberFormatException e) {
            return "층수비교실패 ";
        }

        return "불법 옥탑: 층수 이상 없음 ";
    }
}
