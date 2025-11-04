package com.safe_jeonse.server.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class hoParseUtils {

    /**
     * 호수(hoNm) 문자열에서 층(floor) 번호를 파싱합니다.
     * "일반건축물"(다가구)에서 층수 비교 시 사용됩니다.
     *
     * 예:
     * "505호"  -> 5
     * "1001호" -> 10
     * "301호"  -> 3
     * "B101호" -> -1 (지하 1층)
     * "B201호" -> -2 (지하 2층)
     *
     * @param detailHo 사용자가 입력한 '호수' (e.g., "505호", "B101호")
     * @return 층수 (int)
     */
    public int parseFloorFromHo(String detailHo) {
        if (detailHo == null || detailHo.isEmpty()) {
            return 0; // 또는 예외 처리
        }

        // "호", " ", "B", "b" 외의 모든 특수문자/한글 제거 (e.g., "A-101" -> "A101")
        String ho = detailHo.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        // 1. 지하(B) 처리
        if (ho.startsWith("B")) {
            String floorPart = ho.substring(1); // "B" 제거 (e.g., "101", "201")
            if (floorPart.length() < 3) {
                return -1; // "B1", "B01" 등은 지하 1층 간주
            }
            try {
                // "B101" -> "1" -> -1
                // "B1201" -> "12" -> -12
                String floorStr = floorPart.substring(0, floorPart.length() - 2);
                return -Integer.parseInt(floorStr);
            } catch (Exception e) {
                log.warn("지하 층수 파싱 실패: {}", detailHo, e);
                return -1; // 파싱 실패 시 지하 1층으로 간주
            }
        }

        // 2. 지상층 처리
        String floorPart = ho.replaceAll("[^0-9]", ""); // 숫자만 추출
        if (floorPart.length() < 3) {
            // "101" (3자리) 미만은 층수 구분이 어려움 (e.g., "55"호?)
            // 이 경우 1층으로 간주하거나, 정책에 따라 0 반환
            return 1;
        }

        try {
            // 3. 마지막 두 자리를 "호수"로 보고, 그 앞을 "층수"로 봅니다.
            // "301" -> "3" -> 3
            // "1001" -> "10" -> 10
            String floorStr = floorPart.substring(0, floorPart.length() - 2);
            return Integer.parseInt(floorStr);
        } catch (Exception e) {
            log.warn("지상 층수 파싱 실패: {}", detailHo, e);
            return 0; // "A-101" 등 문자 포함 시 파싱 실패
        }
    }
}
