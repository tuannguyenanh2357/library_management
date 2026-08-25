package com.library.entity;

import com.library.entity.enums.FineStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "fines")

public class Fines {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Mối quan hệ với bảng Borrowing
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrowing_id", nullable = false, unique = true)
    Borrowing borrowing;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    @Nationalized
    @Column(name = "reason", nullable = false)
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    FineStatus status;

    @Column(name = "issued_date", nullable = false)
    LocalDate issuedDate;

    @Column(name = "paid_date")
    LocalDate paidDate;

}
