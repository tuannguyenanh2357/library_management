package com.library.service.interfaces;

import com.library.dto.request.ReservationCreationRequest;
import com.library.dto.response.ReservationResponse;
import com.library.entity.BookCopy;

import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(ReservationCreationRequest request);
    List<ReservationResponse> getMyReservations(String username);
    List<ReservationResponse> getPendingReservationsForBook(Long bookId);
    List<ReservationResponse> getAllReservations();
    void cancelReservation(Long reservationId, String username);
    void completeReservationByCopyId(Long copyId);
    
    // Core logic cho hệ thống: khi có sách trả, tự động gán cho người đang xếp hàng chờ
    void fulfillNextReservationIfAny(Long bookId, BookCopy returnedCopy);
}
