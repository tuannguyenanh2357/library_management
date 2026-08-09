package com.library.repository;

import com.library.entity.Member;
import com.library.dto.response.UnpaidMemberProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.library.entity.enums.FineStatus;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // Kiểm tra xem email đã được đăng ký hay chưa
    boolean existsByEmail(String email);

    // Kiểm tra xem tên đăng nhập đã tồn tại hay chưa
    boolean existsByUsername(String username);

    // Kiểm tra xem số điện thoại đã được sử dụng hay chưa
    boolean existsByPhone(String phone);

    // Tìm kiếm thông tin thành viên theo tên đăng nhập
    Optional<Member> findByUsername(String username);

    // Lấy danh sách các thành viên đang có khoản phạt chưa đóng
    @Query("SELECT m.id AS memberId, m.memberCode AS memberCode, m.name AS name, m.email AS email, m.phone AS phone, "
            +
            "SUM(f.amount) AS totalUnpaidAmount, COUNT(f.id) AS unpaidFinesCount " +
            "FROM Member m " +
            "JOIN m.borrowings br " +
            "JOIN br.fine f " +
            "WHERE f.status = FineStatus.UNPAID " +
            "GROUP BY m.id, m.memberCode, m.name, m.email, m.phone " +
            "ORDER BY SUM(f.amount) DESC")
    List<UnpaidMemberProjection> getMembersWithUnpaidFines();
}
