package com.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // Lỗi chung & Hệ thống
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Mã lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Chưa đăng nhập hoặc phiên làm việc hết hạn", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1003, "ADMIN - Thủ Thư không được mượn sách", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(1004, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),

    // Xác thực & Thành viên
    MEMBER_NOT_FOUND(2001, "Không tìm thấy thông tin độc giả", HttpStatus.NOT_FOUND),
    USER_EXISTED(2002, "Tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(2003, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(2004, "Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(2005, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(2006, "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ADMIN(2007, "Không thể xóa tài khoản Quản trị viên (ADMIN)", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_MEMBER_WITH_BOOKS(2008, "Không thể xóa độc giả vì họ đang mượn sách chưa trả",
            HttpStatus.BAD_REQUEST),

    // Tài nguyên / Thực thể
    RESOURCE_NOT_FOUND(3001, "Không tìm thấy tài nguyên yêu cầu", HttpStatus.NOT_FOUND),
    BOOK_NOT_FOUND(3002, "Không tìm thấy thông tin sách", HttpStatus.NOT_FOUND),
    BOOK_COPY_NOT_FOUND(3003, "Không tìm thấy bản sao sách", HttpStatus.NOT_FOUND),
    BORROWING_NOT_FOUND(3004, "Không tìm thấy thông tin lượt mượn", HttpStatus.NOT_FOUND),
    RESERVATION_NOT_FOUND(3005, "Không tìm thấy thông tin đặt chỗ", HttpStatus.NOT_FOUND),

    // Quy tắc nghiệp vụ
    BOOK_NOT_AVAILABLE(4001, "Sách hiện tại không có sẵn để mượn", HttpStatus.BAD_REQUEST),
    HAS_OVERDUE_BOOKS(4002, "Không thể thao tác khi đang có sách quá hạn chưa trả", HttpStatus.BAD_REQUEST),
    HAS_UNPAID_FINES(4003, "Không thể thao tác khi đang có khoản phạt chưa thanh toán", HttpStatus.BAD_REQUEST),
    RESERVATION_FAILED(4004, "Thao tác đặt chỗ không hợp lệ", HttpStatus.BAD_REQUEST),
    CANNOT_EXTEND_BORROWING(4005, "Không thể gia hạn lượt mượn này", HttpStatus.BAD_REQUEST),
    BORROWING_ACTION_FAILED(4006, "Thao tác mượn/trả sách không hợp lệ", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
