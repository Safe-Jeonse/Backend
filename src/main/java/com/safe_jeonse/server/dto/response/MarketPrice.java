package com.safe_jeonse.server.dto.response;

import lombok.Builder;

@Builder
public record MarketPrice(
        long marketPrice,
        String info
) {}
