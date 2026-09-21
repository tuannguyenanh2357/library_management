package com.library.repository;

import com.library.dto.response.FineResponse;
import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FineRepository extends JpaRepository<Fines, Long>, CustomFineRepository {

       // Kiểm tra một thành viên có đang tồn tại khoản phạt với trạng thái cụ thể
       boolean existsByBorrowingMemberIdAndStatus(Long memberId, FineStatus status);

       // Lấy toàn bộ danh sách phiếu phạt dưới dạng DTO Projection
       @Query("SELECT new com.library.dto.response.FineResponse(" +
                     "f.id, b.id, m.id, m.name, bc.book.title, f.amount, f.reason, f.status, f.issuedDate, f.paidDate) "
                     +
                     "FROM Fines f " +
                     "JOIN f.borrowing b " +
                     "JOIN b.member m " +
                     "JOIN b.bookCopy bc " +
                     "ORDER BY f.id DESC")
       List<FineResponse> findProjectedAll();

       // Lấy danh sách phiếu phạt của một thành viên dưới dạng DTO Projection
       @Query("SELECT new com.library.dto.response.FineResponse(" +
                     "f.id, b.id, m.id, m.name, bc.book.title, f.amount, f.reason, f.status, f.issuedDate, f.paidDate) "
                     +
                     "FROM Fines f " +
                     "JOIN f.borrowing b " +
                     "JOIN b.member m " +
                     "JOIN b.bookCopy bc " +
                     "WHERE m.id = :memberId " +
                     "ORDER BY f.id DESC")
       List<FineResponse> findProjectedByMemberId(@Param("memberId") Long memberId);

       // Lấy danh sách phiếu phạt theo trạng thái dưới dạng DTO Projection
       @Query("SELECT new com.library.dto.response.FineResponse(" +
                     "f.id, b.id, m.id, m.name, bc.book.title, f.amount, f.reason, f.status, f.issuedDate, f.paidDate) "
                     +
                     "FROM Fines f " +
                     "JOIN f.borrowing b " +
                     "JOIN b.member m " +
                     "JOIN b.bookCopy bc " +
                     "WHERE f.status = :status " +
                     "ORDER BY f.id DESC")
       List<FineResponse> findProjectedByStatus(@Param("status") FineStatus status);
}
