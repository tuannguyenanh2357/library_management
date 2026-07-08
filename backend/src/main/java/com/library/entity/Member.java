package com.library.entity;

import com.library.entity.enums.MemberRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.library.entity.enums.BorrowingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(nullable = false)
    String name;

    @Email
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false, unique = true)
    String phone;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    String address;

    @Column(name = "joining_date")
    LocalDate joiningDate;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    MemberRole role;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @Builder.Default
    List<Borrowing> borrowings = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if(joiningDate == null) {
            joiningDate = LocalDate.now();
        }

        if(isActive == null) {
            isActive = true;
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
    public long countOverdueBorrowings(){
        return borrowings.stream().filter(Borrowing::isCurrentlyOverdue).count();
    }

    // lấy danh sách phiếu quá hạn
    public List<Borrowing> getOverdueBorrowings() {
        return borrowings.stream().filter(Borrowing::isCurrentlyOverdue).toList();
    }
}