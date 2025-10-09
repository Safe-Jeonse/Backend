package com.safe_jeonse.server.exception;

import com.safe_jeonse.server.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_DATA_FORMAT", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("잘못된 요청: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(FileParseException.class)
    public ResponseEntity<ErrorResponse> handleFileParse(FileParseException ex) {
        log.error("파일 파싱 오류: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("FILE_PARSE_ERROR", "PDF 분석 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(NotRegistryDocumentException.class)
    public ResponseEntity<ErrorResponse> handleRegistryFileCheck(NotRegistryDocumentException ex) {
        log.warn("등기부 등본이 아닌 파일: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("INVALID_FILE_TYPE", "업로드된 파일이 등기부등본이 아닙니다."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        log.error("파일 업로드 오류: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_FILE_FORMAT", "파일 업로드 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(AiApiException.class)
    public ResponseEntity<ErrorResponse> handleAiApiException(AiApiException ex) {
        log.error("AI API 호출 오류: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("AI_API_ERROR", "AI 분석 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("서버 내부 오류", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
