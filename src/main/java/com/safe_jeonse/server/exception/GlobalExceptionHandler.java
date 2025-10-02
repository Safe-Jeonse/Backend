package com.safe_jeonse.server.exception;

import com.safe_jeonse.server.dto.response.ErrorResponse;
import com.safe_jeonse.server.dto.FieldErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("검증 실패", fieldErrors));
    }
}
