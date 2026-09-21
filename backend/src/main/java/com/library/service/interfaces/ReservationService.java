package com.library.service.interfaces;

import com.library.dto.request.ReservationCreationRequest;
import com.library.dto.response.ReservationResponse;
import com.library.entity.BookCopy;

import java.util.List;

public interface ReservationService {
    // tạo yêu cầu đặt trước một đầu sách
    ReservationResponse createReservation(ReservationCreationRequest request);

    // lấy danh sách đặt trước
    List<ReservationResponse> getMyReservations(String username);

    // lấy danh sách đặt trước đang chờ của một đầu sách
    List<ReservationResponse> getPendingReservationsForBook(Long bookId);

    // lấy toàn bộ danh sách đặt trước
    List<ReservationResponse> getAllReservations();

    // hủy yêu cầu đặt trước của thành viên
    void cancelReservation(Long reservationId, String username);

    // hoàn tất đặt trước khi thành viên đã nhận bản sao được giữ
    void completeReservationByCopyId(Long copyId);

    // tự động gán cho người đang xếp hàng chờ khi có sách
    void fulfillNextReservationIfAny(Long bookId, BookCopy returnedCopy);

    // kiểm tra xem có ai đang xếp hàng chờ đầu sách này không
    boolean hasPendingReservations(Long bookId);
}
