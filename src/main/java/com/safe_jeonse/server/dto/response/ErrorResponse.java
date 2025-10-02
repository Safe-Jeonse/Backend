package com.safe_jeonse.server.dto.response;

import com.safe_jeonse.server.dto.FieldErrorDto;

import java.util.List;

public record ErrorResponse(
        String error,
        List<FieldErrorDto> fieldErrors
) {}
