package com.safe_jeonse.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/*
 추후 행정 안전부 공공 API로 도로명 주소 검증
 */
@Service
@RequiredArgsConstructor
public class AddressValidationService {

    public boolean validateAddress(String address) {
        return true;
    }
}
