package com.library.repository;

import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FineRepository extends JpaRepository<Fines, Long> {

    @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member m "
            + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book "
            + "WHERE b.member.id = :memberId AND f.status = :status")
    List<Fines> findByBorrowing_Member_IdAndStatus(Long memberId, FineStatus status);

    @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member " + "JOIN FETCH b.bookCopy bc "
            + "JOIN FETCH bc.book")
    List<Fines> findAllWithRelations();

    @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member m "
            + "JOIN FETCH b.bookCopy bc " + "JOIN FETCH bc.book " + "WHERE b.member.id = :memberId")
    List<Fines> findByMemberIdWithRelations(@Param("memberId") Long memberId);

    @Query("SELECT f FROM Fines f " + "JOIN FETCH f.borrowing b " + "JOIN FETCH b.member " + "JOIN FETCH b.bookCopy bc "
            + "JOIN FETCH bc.book " + "WHERE f.status = :status")
    List<Fines> findByStatusWithRelations(@Param("status") FineStatus status);
}
