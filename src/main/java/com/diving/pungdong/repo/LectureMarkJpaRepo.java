package com.diving.pungdong.repo;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LectureMarkJpaRepo extends JpaRepository<LectureMark, Long> {
    @Query(
            value = "select lm from LectureMark lm join fetch lm.lecture where lm.account = :account",
            countQuery = "select count(lm) from LectureMark lm"
    )
    Page<LectureMark> findByAccount(@Param("account") Account account, Pageable pageable);

    @Query("select lm from LectureMark lm where lm.account.id = :accountId and lm.lecture.id = :lectureId")
    Optional<LectureMark> findByAccountAndLecture(@Param("accountId") Long accountId, @Param("lectureId") Long lectureId);

    List<LectureMark> findByAccount(Account account);
}
