package com.library.repository;

import com.library.dto.response.ReservationResponse;
import com.library.entity.Reservation;
import com.library.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

       // Tìm yêu cầu đặt trước sớm nhất (người xếp hàng đầu tiên) cho một đầu sách
       // theo trạng thái
       Optional<Reservation> findFirstByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);

       // Lấy toàn bộ danh sách xếp hàng đặt trước của một đầu sách theo trạng thái
       List<Reservation> findByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);

       // Kiểm tra xem một thành viên đã đặt trước cuốn sách này với các trạng thái
       // tương ứng hay chưa
       boolean existsByMemberIdAndBookIdAndStatusIn(Long memberId, Long bookId, List<ReservationStatus> statuses);

       // Tìm reservation đang FULFILLED gắn với một bản sao sách cụ thể
       Optional<Reservation> findFirstByFulfilledCopyIdAndStatus(Long fulfilledCopyId, ReservationStatus status);

       // Kiểm tra xem bản sao sách đang giữ chỗ (FULFILLED) có phải dành cho member
       // này không
       boolean existsByMemberIdAndFulfilledCopyIdAndStatus(Long memberId, Long fulfilledCopyId,
                     ReservationStatus status);

       // Tìm các yêu cầu đặt trước đã được đáp ứng (sách đã về) nhưng người dùng không
       // đến lấy và đã quá hạn, để hệ thống tự động hủy
       @Query("SELECT r FROM Reservation r WHERE r.status = 'FULFILLED' AND r.expiryDate < CURRENT_TIMESTAMP")
       List<Reservation> findExpiredFulfilledReservations();

       boolean existsByBookId(Long bookId);

       // Lấy danh sách yêu cầu đặt trước của thành viên theo username
       @Query("SELECT new com.library.dto.response.ReservationResponse(" +
                     "r.id, m.id, m.name, bk.id, bk.title, bk.imageUrl, " +
                     "fc.id, fc.barCode, r.status, r.requestDate, r.fulfilledDate, r.expiryDate, " +
                     "(SELECT MIN(b.dueDate) FROM Borrowing b WHERE b.bookCopy.book.id = bk.id AND b.status = com.library.entity.enums.BorrowingStatus.ACTIVE AND r.status = com.library.entity.enums.ReservationStatus.PENDING)) "
                     +
                     "FROM Reservation r " +
                     "JOIN r.member m " +
                     "JOIN r.book bk " +
                     "LEFT JOIN r.fulfilledCopy fc " +
                     "WHERE m.username = :username " +
                     "ORDER BY r.requestDate DESC")
       List<ReservationResponse> findProjectedByMemberUsername(@Param("username") String username);

       // Lấy danh sách yêu cầu đặt trước của một sách theo trạng thái
       @Query("SELECT new com.library.dto.response.ReservationResponse(" +
                     "r.id, m.id, m.name, bk.id, bk.title, bk.imageUrl, " +
                     "fc.id, fc.barCode, r.status, r.requestDate, r.fulfilledDate, r.expiryDate, " +
                     "(SELECT MIN(b.dueDate) FROM Borrowing b WHERE b.bookCopy.book.id = bk.id AND b.status = com.library.entity.enums.BorrowingStatus.ACTIVE AND r.status = com.library.entity.enums.ReservationStatus.PENDING)) "
                     +
                     "FROM Reservation r " +
                     "JOIN r.member m " +
                     "JOIN r.book bk " +
                     "LEFT JOIN r.fulfilledCopy fc " +
                     "WHERE bk.id = :bookId AND r.status = :status " +
                     "ORDER BY r.requestDate ASC")
       List<ReservationResponse> findProjectedByBookIdAndStatus(@Param("bookId") Long bookId,
                     @Param("status") ReservationStatus status);

       // Lấy toàn bộ danh sách yêu cầu đặt trước
       @Query("SELECT new com.library.dto.response.ReservationResponse(" +
                     "r.id, m.id, m.name, bk.id, bk.title, bk.imageUrl, " +
                     "fc.id, fc.barCode, r.status, r.requestDate, r.fulfilledDate, r.expiryDate, " +
                     "(SELECT MIN(b.dueDate) FROM Borrowing b WHERE b.bookCopy.book.id = bk.id AND b.status = com.library.entity.enums.BorrowingStatus.ACTIVE AND r.status = com.library.entity.enums.ReservationStatus.PENDING)) "
                     +
                     "FROM Reservation r " +
                     "JOIN r.member m " +
                     "JOIN r.book bk " +
                     "LEFT JOIN r.fulfilledCopy fc " +
                     "ORDER BY r.requestDate DESC")
       List<ReservationResponse> findProjectedAll();
}
