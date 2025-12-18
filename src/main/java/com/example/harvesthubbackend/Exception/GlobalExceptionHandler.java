package com.example.harvesthubbackend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex, WebRequest request) {
        HttpStatus status = getHttpStatus(ex.getErrorCode());
        ApiResponse<Object> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getDetails());
        return ResponseEntity.status(status).body(response);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        // Try to extract error code from message if it's a known error
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();
        
        // Map common runtime exceptions to error codes
        if (message != null) {
            if (message.contains("not found") || message.contains("không tìm thấy")) {
                errorCode = ErrorCode.RESOURCE_NOT_FOUND;
            } else if (message.contains("already exists") || message.contains("đã tồn tại")) {
                errorCode = ErrorCode.USER_ALREADY_EXISTS;
            } else if (message.contains("Invalid") || message.contains("không hợp lệ")) {
                errorCode = ErrorCode.INVALID_REQUEST;
            }
        }
        
        ApiResponse<Object> response = ApiResponse.error(errorCode, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> response = ApiResponse.error(
            ErrorCode.VALIDATION_ERROR, 
            "Lỗi xác thực dữ liệu", 
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
            ErrorCode.AUTH_INVALID_CREDENTIALS,
            "Tên đăng nhập hoặc mật khẩu không đúng"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(
            LockedException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
            ErrorCode.AUTH_ACCOUNT_LOCKED,
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.LOCKED).body(response);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
            ErrorCode.FORBIDDEN,
            "Bạn không có quyền truy cập tài nguyên này"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        // Log the exception for debugging
        ex.printStackTrace();
        
        ApiResponse<Object> response = ApiResponse.error(
            ErrorCode.INTERNAL_SERVER_ERROR,
            "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        int code = errorCode.getCode();
        
        // 1000-1999: General errors -> 500
        if (code >= 1000 && code < 2000) {
            if (code == 1002) return HttpStatus.NOT_FOUND;
            if (code == 1003) return HttpStatus.UNAUTHORIZED;
            if (code == 1004) return HttpStatus.FORBIDDEN;
            if (code == 1005) return HttpStatus.BAD_REQUEST;
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        // 2000-2099: Authentication errors -> 401/423
        if (code >= 2000 && code < 3000) {
            if (code == 2001) return HttpStatus.LOCKED; // 423
            return HttpStatus.UNAUTHORIZED;
        }
        
        // 3000-3999: User errors -> 404/400/409
        if (code >= 3000 && code < 4000) {
            if (code == 3000) return HttpStatus.NOT_FOUND;
            if (code == 3001 || code == 3002 || code == 3003) return HttpStatus.CONFLICT;
            return HttpStatus.BAD_REQUEST;
        }
        
        // 4000-4999: Product errors -> 404/400
        if (code >= 4000 && code < 5000) {
            if (code == 4000) return HttpStatus.NOT_FOUND;
            return HttpStatus.BAD_REQUEST;
        }
        
        // 5000-5999: Order errors -> 404/400
        if (code >= 5000 && code < 6000) {
            if (code == 5000) return HttpStatus.NOT_FOUND;
            return HttpStatus.BAD_REQUEST;
        }
        
        // Default to 400
        return HttpStatus.BAD_REQUEST;
    }
}

