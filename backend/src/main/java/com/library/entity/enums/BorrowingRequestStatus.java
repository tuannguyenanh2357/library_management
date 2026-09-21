package com.library.entity.enums;

public enum BorrowingRequestStatus {
    PENDING,    // Đang chờ Admin duyệt
    APPROVED,   // Đã duyệt, sách đang được giữ chỗ - chờ độc giả đến lấy
    REJECTED,   // Admin từ chối
    CANCELLED,  // Độc giả tự hủy
    COMPLETED,  // Độc giả đã đến lấy, đã tạo phiếu mượn thực tế
    EXPIRED     // Quá hạn lấy sách, đã hủy và trả sách về kho
}
