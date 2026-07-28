package com.library.repository;

import com.library.entity.BorrowingRequest;
import com.library.entity.enums.BorrowingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowingRequestRepository extends JpaRepository<BorrowingRequest, Long> {

    @Query("SELECT br FROM BorrowingRequest br JOIN FETCH br.member JOIN FETCH br.book WHERE br.status = :status ORDER BY br.requestDate DESC")
    List<BorrowingRequest> findByStatusWithRelations(BorrowingRequestStatus status);

    @Query("SELECT br FROM BorrowingRequest br JOIN FETCH br.member JOIN FETCH br.book WHERE br.status != 'PENDING' ORDER BY br.processedDate DESC")
    List<BorrowingRequest> findHistoryWithRelations();

    @Query("SELECT br FROM BorrowingRequest br JOIN FETCH br.member JOIN FETCH br.book WHERE br.member.id = :memberId ORDER BY br.requestDate DESC")
    List<BorrowingRequest> findByMemberIdWithRelations(Long memberId);

    // Tìm các request PENDING quá lâu chưa được xử lý, để cronjob tự động hủy
    @Query("SELECT br FROM BorrowingRequest br WHERE br.status = 'PENDING' AND br.requestDate < :cutoff")
    List<BorrowingRequest> findStalePendingRequests(@Param("cutoff") LocalDateTime cutoff);
}
