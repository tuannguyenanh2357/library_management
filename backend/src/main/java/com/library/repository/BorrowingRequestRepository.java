package com.library.repository;

import com.library.entity.BorrowingRequest;
import com.library.entity.enums.BorrowingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowingRequestRepository extends JpaRepository<BorrowingRequest, Long> {

    // Lấy danh sách yêu cầu mượn theo trạng thái và tải sẵn thông tin thành viên
    // đầu sách để phục vụ hiển thị.
    @Query("SELECT br FROM BorrowingRequest br JOIN FETCH br.member JOIN FETCH br.book WHERE br.status = :status ORDER BY br.requestDate DESC")
    List<BorrowingRequest> findByStatusWithRelations(BorrowingRequestStatus status);

    // Lấy lịch sử các yêu cầu đã được xử lý và tải sẵn các thông tin liên quan.
    @Query("SELECT br FROM BorrowingRequest br JOIN FETCH br.member JOIN FETCH br.book WHERE br.status != 'PENDING' ORDER BY br.processedDate DESC")
    List<BorrowingRequest> findHistoryWithRelations();

    // Lấy danh sách yêu cầu mượn của một thành viên và tải sẵn thông tin thành
    // viên, đầu sách.
    @Query("SELECT br FROM BorrowingRequest br JOIN FETCH br.member JOIN FETCH br.book WHERE br.member.id = :memberId ORDER BY br.requestDate DESC")
    List<BorrowingRequest> findByMemberIdWithRelations(Long memberId);

    // Lấy các yêu cầu đang chờ xử lý nhưng đã quá thời gian quy định để cron job tự
    // động hủy.
    @Query("SELECT br FROM BorrowingRequest br WHERE br.status = 'PENDING' AND br.requestDate < :cutoff")
    List<BorrowingRequest> findStalePendingRequests(@Param("cutoff") LocalDateTime cutoff);
}
