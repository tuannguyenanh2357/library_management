package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "books")

public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Nationalized
    @Column(nullable = false)
    String title;

    @Nationalized
    @Column(nullable = false)
    String author;

    @Nationalized
    @Column(nullable = false)
    String publisher;

    @Nationalized
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
    @Builder.Default
    java.math.BigDecimal dailyFineAmount = new java.math.BigDecimal("5000.00");

    @Column(name = "publication_year")
    Integer publicationYear;
    @Column(name = "created_at")
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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