package com.medipoint.dto;

import java.time.Instant;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    private String errorCode;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, String timestamp, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.errorCode = errorCode;
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now().toString(), null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok("Operation completed successfully.", data);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, Instant.now().toString(), errorCode);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
