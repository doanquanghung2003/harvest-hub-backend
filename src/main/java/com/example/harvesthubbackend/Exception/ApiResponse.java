package com.example.harvesthubbackend.Exception;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorInfo error;
    private LocalDateTime timestamp;
    
    // Success response
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = "Thành công";
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    // Error response
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = errorCode.getMessage();
        response.error = new ErrorInfo(errorCode.getCode(), errorCode.getMessage(), null);
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message != null ? message : errorCode.getMessage();
        response.error = new ErrorInfo(errorCode.getCode(), message != null ? message : errorCode.getMessage(), null);
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, Object details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message != null ? message : errorCode.getMessage();
        response.error = new ErrorInfo(errorCode.getCode(), message != null ? message : errorCode.getMessage(), details);
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public ErrorInfo getError() {
        return error;
    }
    
    public void setError(ErrorInfo error) {
        this.error = error;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Error info inner class
    public static class ErrorInfo {
        private int code;
        private String message;
        private Object details;
        
        public ErrorInfo(int code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }
        
        public int getCode() {
            return code;
        }
        
        public void setCode(int code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Object getDetails() {
            return details;
        }
        
        public void setDetails(Object details) {
            this.details = details;
        }
    }
}

