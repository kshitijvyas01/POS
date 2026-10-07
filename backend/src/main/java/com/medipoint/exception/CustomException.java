package com.medipoint.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public CustomException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static CustomException unauthorized(String message) {
        return new CustomException(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public static CustomException forbidden(String message) {
        return new CustomException(message, HttpStatus.FORBIDDEN, "FORBIDDEN_ACCESS");
    }

    public static CustomException badRequest(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public static CustomException conflict(String message) {
        return new CustomException(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public static CustomException notFound(String message) {
        return new CustomException(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public static CustomException internalError(String message) {
        return new CustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
}
