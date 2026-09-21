package com.library.entity.enums;

public enum ReservationStatus {
    PENDING, // Đang chờ xếp hàng
    FULFILLED, // Đã có sách (đã giữ chỗ sách thành công)
    COMPLETED, // Đã đến lấy sách (đã chuyển thành Borrowing)
    CANCELLED, // Bị hủy (do người dùng hủy hoặc admin hủy)
    EXPIRED // Quá hạn lấy sách (sau khi fulfilled)
}
