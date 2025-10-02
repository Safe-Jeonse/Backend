package com.safe_jeonse.server.dto;

public record FieldErrorDto(
        String field,
        String message
) {}