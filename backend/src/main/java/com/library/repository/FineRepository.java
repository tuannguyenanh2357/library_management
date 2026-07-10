package com.library.repository;

import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FineRepository extends JpaRepository<Fines, Long> {
    List<Fines> findByBorrowing_Member_Id(Long memberId);
    List<Fines> findByBorrowing_Member_IdAndStatus(Long memberId, FineStatus status);
    List<Fines> findByStatus(FineStatus status);
}
