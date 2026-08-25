package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "books", indexes = {
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_author", columnList = "author"),
        @Index(name = "idx_book_category", columnList = "category")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Nationalized
    @Column(nullable = false, length = 40)
    String title;

    @Nationalized
    @Column(nullable = false)
    String author;

    @Nationalized
    @Column(nullable = false)
    String publisher;

    @Column(nullable = false, unique = true)
    String isbn;

    @Nationalized
    String category;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "daily_fine_amount", precision = 10, scale = 2)
    BigDecimal dailyFineAmount;

    // Phí đền bù khi bản sao của sách này bị báo mất/hỏng
    @Column(name = "replacement_fee", precision = 10, scale = 2)
    BigDecimal replacementFee;

    @Column(name = "publication_year")
    Integer publicationYear;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    List<BookCopy> copies = new ArrayList<>();

    public void addCopy(BookCopy copy) {
        copies.add(copy);
        copy.setBook(this);
    }

    public void removeCopy(BookCopy copy) {
        copies.remove(copy);
        copy.setBook(null);
    }
}