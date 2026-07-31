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
    
    // Lấy danh sách yêu cầu đặt trước của một thành viên, sắp xếp mới nhất lên đầu
    List<Reservation> findByMemberIdOrderByRequestDateDesc(Long memberId);
    
    // Tìm yêu cầu đặt trước sớm nhất (người xếp hàng đầu tiên) cho một đầu sách theo trạng thái
    Optional<Reservation> findFirstByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);
    
    // Lấy toàn bộ danh sách xếp hàng đặt trước của một đầu sách theo trạng thái
    List<Reservation> findByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);

    // Kiểm tra xem một thành viên đã đặt trước cuốn sách này với các trạng thái tương ứng hay chưa
    boolean existsByMemberIdAndBookIdAndStatusIn(Long memberId, Long bookId, List<ReservationStatus> statuses);
    
    // Tìm các yêu cầu đặt trước đã được đáp ứng (sách đã về) nhưng người dùng không đến lấy và đã quá hạn, để hệ thống tự động hủy
    @Query("SELECT r FROM Reservation r WHERE r.status = 'FULFILLED' AND r.expiryDate < CURRENT_TIMESTAMP")
    List<Reservation> findExpiredFulfilledReservations();
}
