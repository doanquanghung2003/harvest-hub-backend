package com.example.harvesthubbackend.Exception;

public enum ErrorCode {
    // General errors (1000-1999)
    INTERNAL_SERVER_ERROR(1000, "Lỗi hệ thống nội bộ"),
    INVALID_REQUEST(1001, "Yêu cầu không hợp lệ"),
    RESOURCE_NOT_FOUND(1002, "Không tìm thấy tài nguyên"),
    UNAUTHORIZED(1003, "Không có quyền truy cập"),
    FORBIDDEN(1004, "Bị cấm truy cập"),
    VALIDATION_ERROR(1005, "Lỗi xác thực dữ liệu"),
    
    // Authentication errors (2000-2099)
    AUTH_INVALID_CREDENTIALS(2000, "Tên đăng nhập hoặc mật khẩu không đúng"),
    AUTH_ACCOUNT_LOCKED(2001, "Tài khoản đã bị khóa"),
    AUTH_PASSWORD_EXPIRED(2002, "Mật khẩu đã hết hạn"),
    AUTH_TOKEN_INVALID(2003, "Token không hợp lệ"),
    AUTH_TOKEN_EXPIRED(2004, "Token đã hết hạn"),
    AUTH_PASSWORD_WEAK(2005, "Mật khẩu không đủ mạnh"),
    
    // User errors (3000-3099)
    USER_NOT_FOUND(3000, "Không tìm thấy người dùng"),
    USER_ALREADY_EXISTS(3001, "Người dùng đã tồn tại"),
    USER_EMAIL_EXISTS(3002, "Email đã được sử dụng"),
    USER_USERNAME_EXISTS(3003, "Tên đăng nhập đã được sử dụng"),
    USER_ACCOUNT_DISABLED(3004, "Tài khoản đã bị vô hiệu hóa"),
    
    // Product errors (4000-4099)
    PRODUCT_NOT_FOUND(4000, "Không tìm thấy sản phẩm"),
    PRODUCT_ALREADY_EXISTS(4001, "Sản phẩm đã tồn tại"),
    PRODUCT_OUT_OF_STOCK(4002, "Sản phẩm đã hết hàng"),
    PRODUCT_INSUFFICIENT_STOCK(4003, "Số lượng sản phẩm không đủ"),
    PRODUCT_APPROVAL_INVALID(4004, "Không thể duyệt/từ chối sản phẩm với trạng thái hiện tại"),
    PRODUCT_REJECTION_REASON_REQUIRED(4005, "Lý do từ chối là bắt buộc"),

    // Review errors (4100-4199)
    REVIEW_ALREADY_EXISTS(4100, "Bạn đã đánh giá sản phẩm này"),
    REVIEW_ORDER_NOT_DELIVERED(4101, "Chỉ có thể đánh giá khi đơn hàng đã giao thành công"),
    REVIEW_PRODUCT_NOT_IN_ORDER(4102, "Sản phẩm không thuộc đơn hàng này"),
    
    // Order errors (5000-5099)
    ORDER_NOT_FOUND(5000, "Không tìm thấy đơn hàng"),
    ORDER_STATUS_INVALID(5001, "Chuyển đổi trạng thái đơn hàng không hợp lệ"),
    ORDER_CANNOT_CANCEL(5002, "Không thể hủy đơn hàng với trạng thái hiện tại"),
    ORDER_CANNOT_RETURN(5003, "Chỉ đơn hàng đã giao mới có thể trả lại"),
    ORDER_CANNOT_REFUND(5004, "Không thể hoàn tiền cho đơn hàng với trạng thái hiện tại"),
    ORDER_EMPTY_CART(5005, "Giỏ hàng trống, không thể đặt hàng"),
    
    // Cart errors (6000-6099)
    CART_NOT_FOUND(6000, "Không tìm thấy giỏ hàng"),
    CART_ITEM_NOT_FOUND(6001, "Không tìm thấy sản phẩm trong giỏ hàng"),
    CART_INVALID_QUANTITY(6002, "Số lượng sản phẩm không hợp lệ"),
    
    // Payment errors (7000-7099)
    PAYMENT_NOT_FOUND(7000, "Không tìm thấy giao dịch thanh toán"),
    PAYMENT_ALREADY_PROCESSED(7001, "Giao dịch đã được xử lý"),
    PAYMENT_FAILED(7002, "Thanh toán thất bại"),
    PAYMENT_INSUFFICIENT_BALANCE(7003, "Số dư không đủ"),
    
    // Seller errors (8000-8099)
    SELLER_NOT_FOUND(8000, "Không tìm thấy người bán"),
    SELLER_NOT_APPROVED(8001, "Người bán chưa được duyệt"),
    SELLER_ALREADY_EXISTS(8002, "Người bán đã tồn tại"),
    
    // File upload errors (9000-9099)
    FILE_UPLOAD_FAILED(9000, "Tải lên tệp thất bại"),
    FILE_TOO_LARGE(9001, "Tệp quá lớn"),
    FILE_INVALID_TYPE(9002, "Loại tệp không hợp lệ"),
    FILE_NOT_FOUND(9003, "Không tìm thấy tệp");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

