package com.safe_jeonse.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
    건축물 대장 표제부, 전유부 조회를 통한 분석 비지니스 로직
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BuildingLedgerAnalysisService {

    private final JusoApiClient jusoApiClient;

    Object buildingLedgerResult(String address){
        jusoApiClient.getJusoCode(address);
        return null;
    }

}
