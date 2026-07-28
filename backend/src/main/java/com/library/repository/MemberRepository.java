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
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    Optional<Member> findByUsername(String username);

    @Query(value = "EXEC dbo.GetMembersWithUnpaidFines", nativeQuery = true)
    List<UnpaidMemberProjection> getMembersWithUnpaidFines();
}
