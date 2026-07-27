package com.library.entity;

import com.library.entity.enums.MemberRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.library.entity.enums.BorrowingStatus;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
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
    String memberCode;

    @NotBlank(message = "Name is required")
    @Nationalized
    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true, length = 50)
    String username;

    @Column(nullable = false, length = 60)
    String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+84|0)[3-9]\\d{8}$", message = "Invalid Vietnamese phone number")
    @Column(nullable = false, unique = true)
    String phone;

    @NotBlank(message = "Address is required")
    @Nationalized
    @Column(nullable = false)
    String address;

    @Column(name = "joining_date")
    LocalDate joiningDate;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String avatar;
    Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    MemberRole role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Builder.Default
    List<Borrowing> borrowings = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Builder.Default
    List<BorrowingRequest> borrowingRequests = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (joiningDate == null) {
            joiningDate = LocalDate.now();
        }

        if (isActive == null) {
            isActive = true;
        }

        if (memberCode == null || memberCode.isBlank()) {
            memberCode = "MBR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        if (role == null) {
            role = MemberRole.MEMBER;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

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