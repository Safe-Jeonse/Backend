package com.safe_jeonse.server.exception;

public class AiApiException extends RuntimeException {
    public AiApiException(String message) {
        super(message);
    }

    public AiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

