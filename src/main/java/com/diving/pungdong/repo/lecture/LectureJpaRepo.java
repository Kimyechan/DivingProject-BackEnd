package com.diving.pungdong.repo.lecture;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LectureJpaRepo extends JpaRepository<Lecture, Long>, LectureJpaRepoCustom {
    Page<Lecture> findByRegion(String region, Pageable pageable);

    Page<Lecture> findByInstructor(Account instructor, Pageable pageable);

    @Query(
            value = "select l from Lecture l where l.registrationDate > :pastDate",
            countQuery = "select count(l) from Lecture l where l.registrationDate > :pastDate"
    )
    Page<Lecture> findFromPastDate(@Param("pastDate") LocalDate pastDate, Pageable pageable);

    Page<Lecture> findLectureByRegistrationDateAfter(LocalDate pastDate, Pageable pageable);
}