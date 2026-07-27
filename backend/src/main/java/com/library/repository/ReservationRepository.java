package com.library.repository;

import com.library.entity.Reservation;
import com.library.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Tìm reservation của member
    List<Reservation> findByMemberIdOrderByRequestDateDesc(Long memberId);
    
    // Tìm người đặt sớm nhất cho một đầu sách đang chờ (PENDING)
    Optional<Reservation> findFirstByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);
    
    // Tìm các đặt chỗ đang chờ của 1 quyển sách
    List<Reservation> findByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);

    // Kiểm tra xem độc giả đã đặt sách này chưa
    boolean existsByMemberIdAndBookIdAndStatusIn(Long memberId, Long bookId, List<ReservationStatus> statuses);
    
    // Tìm các reservation quá hạn để cronjob hủy (EXPIRED)
    @Query("SELECT r FROM Reservation r WHERE r.status = 'FULFILLED' AND r.expiryDate < CURRENT_TIMESTAMP")
    List<Reservation> findExpiredFulfilledReservations();
}
