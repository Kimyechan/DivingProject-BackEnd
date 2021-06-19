package com.diving.pungdong.repo;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureMarkJpaRepo extends JpaRepository<LectureMark, Long> {
    @Query(
            value = "select lm from LectureMark lm join fetch lm.lecture where lm.account = :account",
            countQuery = "select count(lm) from LectureMark lm"
    )
    Page<LectureMark> findByAccount(@Param("account") Account account, Pageable pageable);
}
