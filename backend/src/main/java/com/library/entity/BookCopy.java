package com.library.entity;

import com.library.entity.enums.BookCopyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    @Builder.Default
    BookCopyStatus status = BookCopyStatus.AVAILABLE;

    // String shelfLocation;

    @OneToMany(mappedBy = "bookCopy", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Builder.Default
    List<Borrowing> borrowings = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

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
