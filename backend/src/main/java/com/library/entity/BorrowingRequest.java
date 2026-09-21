package com.library.entity;

import com.library.entity.enums.BorrowingRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "borrowing_requests", indexes = {
        @Index(name = "idx_borrowingrequest_notes", columnList = "notes")
})
public class BorrowingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    BorrowingRequestStatus status = BorrowingRequestStatus.PENDING;

    @CreatedDate
    @Column(name = "request_date", nullable = false, updatable = false)
    LocalDateTime requestDate;

    @Column(name = "expected_due_date")
    LocalDate expectedDueDate;

    @Column(name = "processed_date")
    LocalDateTime processedDate;

    @Nationalized
    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    String notes;

    // ID của BookCopy được giữ chỗ sau khi Admin duyệt
    @Column(name = "assigned_book_copy_id")
    Long assignedBookCopyId;

    // Thời điểm Admin duyệt (dùng để kiểm tra quá hạn lấy sách)
    @Column(name = "approved_date")
    LocalDateTime approvedDate;

}

