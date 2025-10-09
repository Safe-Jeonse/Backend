package com.safe_jeonse.server.dto.response;

public record ErrorResponse(
        String errorCode,
        String message
) {}
