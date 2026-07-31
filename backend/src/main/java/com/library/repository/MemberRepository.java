package com.library.repository;

import com.library.entity.Member;
import com.library.dto.response.UnpaidMemberProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // Kiểm tra xem email đã được đăng ký hay chưa
    boolean existsByEmail(String email);
    
    // Kiểm tra xem tên đăng nhập đã tồn tại hay chưa
    boolean existsByUsername(String username);
    
    // Kiểm tra xem số điện thoại đã được sử dụng hay chưa
    boolean existsByPhone(String phone);
    
    // Tìm kiếm thông tin thành viên theo tên đăng nhập
    Optional<Member> findByUsername(String username);

    // Gọi Stored Procedure để lấy danh sách các thành viên đang có khoản phạt chưa đóng
    @Query(value = "EXEC dbo.GetMembersWithUnpaidFines", nativeQuery = true)
    List<UnpaidMemberProjection> getMembersWithUnpaidFines();
}
