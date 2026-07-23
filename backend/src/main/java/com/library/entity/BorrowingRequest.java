package com.library.entity;

import com.library.entity.enums.BorrowingRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "borrowing_requests")
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
    BorrowingRequestStatus status;

    @Column(name = "request_date", nullable = false)
    LocalDateTime requestDate;

    @Column(name = "expected_due_date")
    LocalDate expectedDueDate;

    @Column(name = "processed_date")
    LocalDateTime processedDate;

    @Nationalized
    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    String notes;

    @PrePersist
    protected void prePersist() {
        if (this.requestDate == null) {
            this.requestDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = BorrowingRequestStatus.PENDING;
        }
    }
}
