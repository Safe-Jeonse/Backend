package com.safe_jeonse.server.dto;

import lombok.Builder;

@Builder
public record BuildingAddressInfo(
        String address,
        String lnbrMnnm,
        String lnbrSlno,
        String sigunguCd,
        String bjdongCd,
        String dong,
        String ho) {
}
