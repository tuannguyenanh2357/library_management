package com.library.entity;

import com.library.entity.enums.BookCopyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("unused")
@Table(name = "book_copies")
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 40)
    String barCode;

    // book copy thuộc về cuốn sách nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookCopyStatus status;

    // String shelfLocation;

    @OneToMany(mappedBy = "bookCopy", fetch = FetchType.LAZY)
    @Builder.Default
    List<Borrowing> borrowings = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    LocalDate createdAt;

    @Column(name = "updated_at")
    LocalDate updatedAt;

    @PrePersist
     protected void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();

        if(status == null) {
            this.status = BookCopyStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    // kiểm tra có thể mượn được không
    public boolean isAvailable() {
        return status == BookCopyStatus.AVAILABLE;
    }

    // đánh dấu đang được mượn
    public void markAsBorrowed() {
        this.status = BookCopyStatus.BORROWED;
    }

    // đánh dấu đã trả
    public void markAsReturned() {
        this.status = BookCopyStatus.AVAILABLE;
    }

    // đánh dấu bị mất
    public void markAsLost() {
        this.status = BookCopyStatus.LOST;
    }

    // đánh dấu bị hỏng
    public void markAsDamaged() {
        this.status = BookCopyStatus.DAMAGED;
    }

    // lịch sử mượn
    public void addBorrowing(Borrowing borrowing) {
        borrowings.add(borrowing);
        borrowing.setBookCopy(this);
    }

}
