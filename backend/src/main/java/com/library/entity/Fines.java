package com.library.entity;

import com.library.entity.enums.FineStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;
import com.library.dto.response.report.WeeklyRevenueReportResponse.TopOffenderEntry;

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
@SqlResultSetMapping (
    name = "TopOffendersMapping",
    classes = @ConstructorResult (
        targetClass = TopOffenderEntry.class,
        columns = {
            @ColumnResult(name = "member_id", type = Long.class),
            @ColumnResult(name = "member_name", type = String.class),
            @ColumnResult(name = "email", type = String.class),
            @ColumnResult(name = "fine_count", type = Integer.class),
            @ColumnResult(name = "total_amount", type = BigDecimal.class),
            @ColumnResult(name = "paid_amount", type = BigDecimal.class),
            @ColumnResult(name = "unpaid_amount", type = BigDecimal.class)
        }
    )
)
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
