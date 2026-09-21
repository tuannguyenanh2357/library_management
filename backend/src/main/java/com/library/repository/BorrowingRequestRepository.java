package com.library.repository;

import com.library.dto.response.BorrowingRequestResponse;
import com.library.entity.BorrowingRequest;
import com.library.entity.enums.BorrowingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowingRequestRepository extends JpaRepository<BorrowingRequest, Long> {


    // Lấy các yêu cầu đang chờ xử lý nhưng đã quá thời gian quy định để cron job tự động hủy.
    @Query("SELECT br FROM BorrowingRequest br WHERE br.status = 'PENDING' AND br.requestDate < :cutoff")
    List<BorrowingRequest> findStalePendingRequests(@Param("cutoff") LocalDateTime cutoff);

    // Các câu lệnh mới dùng Class-based Projection
    // Lấy danh sách yêu cầu mượn theo trạng thái dưới dạng DTO Projection
    @Query("SELECT new com.library.dto.response.BorrowingRequestResponse(" +
           "br.id, br.member.id, br.member.name, br.book.id, br.book.title, br.status, " +
           "br.requestDate, br.expectedDueDate, br.processedDate, br.notes, " +
           "(SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book = br.book AND bc.status = com.library.entity.enums.BookCopyStatus.AVAILABLE), " +
           "br.assignedBookCopyId, br.approvedDate) " +
           "FROM BorrowingRequest br " +
           "WHERE br.status = :status " +
           "ORDER BY br.requestDate DESC")
    List<BorrowingRequestResponse> findProjectedByStatus(@Param("status") BorrowingRequestStatus status);

    // Lấy lịch sử các yêu cầu mượn đã xử lý dưới dạng DTO Projection
    @Query("SELECT new com.library.dto.response.BorrowingRequestResponse(" +
           "br.id, br.member.id, br.member.name, br.book.id, br.book.title, br.status, " +
           "br.requestDate, br.expectedDueDate, br.processedDate, br.notes, " +
           "(SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book = br.book AND bc.status = com.library.entity.enums.BookCopyStatus.AVAILABLE), " +
           "br.assignedBookCopyId, br.approvedDate) " +
           "FROM BorrowingRequest br " +
           "WHERE br.status IN (com.library.entity.enums.BorrowingRequestStatus.REJECTED, " +
           "com.library.entity.enums.BorrowingRequestStatus.CANCELLED, " +
           "com.library.entity.enums.BorrowingRequestStatus.COMPLETED, " +
           "com.library.entity.enums.BorrowingRequestStatus.EXPIRED) " +
           "ORDER BY br.processedDate DESC")
    List<BorrowingRequestResponse> findProjectedHistory();

    // Lấy danh sách yêu cầu mượn của một thành viên dưới dạng DTO Projection
    @Query("SELECT new com.library.dto.response.BorrowingRequestResponse(" +
           "br.id, br.member.id, br.member.name, br.book.id, br.book.title, br.status, " +
           "br.requestDate, br.expectedDueDate, br.processedDate, br.notes, " +
           "(SELECT COUNT(bc) FROM BookCopy bc WHERE bc.book = br.book AND bc.status = com.library.entity.enums.BookCopyStatus.AVAILABLE), " +
           "br.assignedBookCopyId, br.approvedDate) " +
           "FROM BorrowingRequest br " +
           "WHERE br.member.id = :memberId " +
           "ORDER BY br.requestDate DESC")
    List<BorrowingRequestResponse> findProjectedByMemberId(@Param("memberId") Long memberId);

    boolean existsByBookId(Long bookId);
}
