package com.library.entity;

import com.library.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id")
    BookCopy fulfilledCopy; // Bản sao được gán khi có người trả sách

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    ReservationStatus status = ReservationStatus.PENDING;

    @CreatedDate
    @Column(name = "request_date", nullable = false, updatable = false)
    LocalDateTime requestDate;

    @Column(name = "fulfilled_date")
    LocalDateTime fulfilledDate; // Ngày có sách (gán fulfilledCopy)

    @Column(name = "expiry_date")
    LocalDateTime expiryDate; // Hạn cuối để đến lấy sách

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

}
