package com.library.repository;

import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FineRepository extends JpaRepository<Fines, Long> {

        // Tìm danh sách phiếu phạt của một thành viên theo trạng thái phạt
        @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member m "
                        + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book "
                        + "WHERE b.member.id = :memberId AND f.status = :status")
        List<Fines> findByBorrowing_Member_IdAndStatus(Long memberId, FineStatus status);

        // Lấy danh sách phạt và tải sẵn các thông tin liên quan để tối ưu truy vấn
        @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member "
                        + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book")
        List<Fines> findAllWithRelations();

        // Lấy danh sách phạt của một thành viên cụ thể kèm theo thông tin chi tiết
        @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member m "
                        + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book " + "WHERE b.member.id = :memberId")
        List<Fines> findByMemberIdWithRelations(@Param("memberId") Long memberId);

        // Lấy danh sách phạt theo trạng thái kèm theo thông tin chi tiết
        @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member "
                        + "JOIN FETCH b.bookCopy bc "
                        + "JOIN FETCH bc.book " + "WHERE f.status = :status")
        List<Fines> findByStatusWithRelations(@Param("status") FineStatus status);

        // Kiểm tra một thành viên có đang tồn tại khoản phạt với trạng thái cụ thể
        boolean existsByBorrowingMemberIdAndStatus(Long memberId, FineStatus status);
}
