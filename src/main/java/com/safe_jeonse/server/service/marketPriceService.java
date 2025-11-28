package com.safe_jeonse.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
/*
    부동산 실 거래가 조회 비지니스 로직
 */
public class marketPriceService {

    @Value("${spring.open-data.open-data-key}")
    private String apiKey;


}
