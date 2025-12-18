package com.example.harvesthubbackend.Exception;

public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object details;
    
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public ApiException(ErrorCode errorCode, String message) {
        super(message != null ? message : errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public ApiException(ErrorCode errorCode, String message, Object details) {
        super(message != null ? message : errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public Object getDetails() {
        return details;
    }
}

