package com.library.entity;

import com.library.entity.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.library.entity.enums.BorrowingStatus;
import org.hibernate.annotations.Nationalized;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "members")

public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "member_code", nullable = false, unique = true)
    @Builder.Default
    String memberCode = "MBR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Nationalized
    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true, length = 50)
    String username;

    @Column(nullable = false, length = 60)
    String password;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, unique = true)
    String phone;

    @Nationalized
    @Column(nullable = false)
    String address;

    @Column(name = "joining_date")
    @Builder.Default
    LocalDate joiningDate = LocalDate.now();

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    MemberRole role = MemberRole.MEMBER;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Builder.Default
    List<Borrowing> borrowings = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Builder.Default
    List<BorrowingRequest> borrowingRequests = new ArrayList<>();

    // @ManyToMany
    // @JoinTable(name = "member_favorite_books", joinColumns = @JoinColumn(name ="member_id"), inverseJoinColumns = @JoinColumn(name = "book_id"))
    // @Builder.Default
    // Set<Book> favoriteBooks = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // xem có đang mượn sách nào chua trả hay không
    public boolean hasBorrowedBooks() {
        return borrowings.stream().anyMatch(borrowing -> borrowing.getStatus() == BorrowingStatus.ACTIVE);
    }

    // có phiếu quá hạn không
    public boolean hasOverdueBorrowings() {
        return borrowings.stream().anyMatch(Borrowing::isCurrentlyOverdue);
    }

    // có tổng bao nhiêu phiếu quá hạn
    public long countOverdueBorrowings() {
        return borrowings.stream().filter(Borrowing::isCurrentlyOverdue).count();
    }

    // lấy danh sách phiếu quá hạn
    public List<Borrowing> getOverdueBorrowings() {
        return borrowings.stream().filter(Borrowing::isCurrentlyOverdue).toList();
    }
}