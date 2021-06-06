package com.diving.pungdong.repo.lecture;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LectureJpaRepo extends JpaRepository<Lecture, Long>, LectureJpaRepoCustom {
    Page<Lecture> findByRegion(String region, Pageable pageable);

    Page<Lecture> findByInstructor(Account instructor, Pageable pageable);

    @Query(
            value = "select l from Lecture l where l.registrationDate > :pastDateTime",
            countQuery = "select count(l) from Lecture l where l.registrationDate > :pastDateTime"
    )
    Page<Lecture> findFromPastDate(@Param("pastDateTime") LocalDateTime pastDateTime, Pageable pageable);

    Page<Lecture> findLectureByRegistrationDateAfter(LocalDateTime pastDateTime, Pageable pageable);

    @Query(
            value = "select l from Lecture l order by l.reviewCount desc, l.reviewTotalAvg desc",
            countQuery = "select count(l) from Lecture l"
    )
    Page<Lecture> findPopularLectures(Pageable pageable);
}