package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.response.MarketPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.safe_jeonse.server.dto.BuildingAddressInfo;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
/*
 * 부동산 실 거래가 조회 비지니스 로직
 */
public class marketPriceService {

    private final DagaguAnalysisService dagaguAnalysisService;
    private final VillaAnalysisService villaAnalysisService;

    public MarketPrice getMarketPrice(BuildingAddressInfo addressInfo, String isApartment, int hhldCnt,
                                      int fmlyCnt) {

        MarketPrice marketPrice = null;
        // 아파트, 다세대 주택
        if (isApartment.equals("Y")) {
            if (containsVillaKeyword(addressInfo.address())) {
                marketPrice = villaAnalysisService.getPublicPrice(addressInfo);
            } else {

            }
        }
        // 다가구 주택
        else {
            marketPrice = dagaguAnalysisService.getPublicPrice(addressInfo, hhldCnt, fmlyCnt);
        }
        return marketPrice;
    }

    private boolean containsVillaKeyword(String name) {
        if (name == null)
            return true; // 이름 없으면 빌라로 간주
        String n = name.trim();
        return n.contains("빌라") || n.contains("다세대") ||
                n.contains("연립") || n.contains("맨션") || n.contains("주택");
    }
}
