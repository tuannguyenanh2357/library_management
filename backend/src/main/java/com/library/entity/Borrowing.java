package com.library.entity;

import com.library.entity.enums.BorrowingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "borrowings")
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // thành viên mượn sách
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @OneToOne(mappedBy = "borrowing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Fines fine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false)
    BookCopy bookCopy;

    // Ngày mượn
    @Column(name = "borrow_date", nullable = false)
    LocalDate borrowDate;

    // Hạn trả
    @Column(name = "due_date", nullable = false)
    LocalDate dueDate;

    // Ngày trả thực tế
    @Column(name = "return_date")
    LocalDate returnDate;

    // Trạng thái mượn sách
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    BorrowingStatus status;

    @Column(name = "created_at", updatable = false)
    LocalDate createdAt;

    @Column(name = "updated_at")
    LocalDate updatedAt;

    // Số lần đã gia hạn (tối đa 1 lần/lượt mượn)
    @Column(name = "renewal_count")
    @Builder.Default
    Integer renewalCount = 0;

    public Integer getRenewalCount() {
        return renewalCount != null ? renewalCount : 0;
    }

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        if (status == null) {
            this.status = BorrowingStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    // kiem tra phiếu mượn có đang quá hạn hay không
    public boolean isCurrentlyOverdue() {
        return status == BorrowingStatus.ACTIVE
                && LocalDate.now().isAfter(dueDate);
    }

    // kiểm tra xem sách đã được trả trễ hay không
    public boolean wasReturnedLate() {
        return returnDate != null
                && returnDate.isAfter(dueDate);
    }

    // số ngày trả trễ
    public long getDaysLate() {
        if(!wasReturnedLate()){
            return 0;
        }

        return ChronoUnit.DAYS.between(dueDate, returnDate);
    }

    // trả sách
    public void returnBook(){
        this.returnDate = LocalDate.now();
        this.status = BorrowingStatus.RETURNED;
        this.bookCopy.markAsReturned();
    }
}

