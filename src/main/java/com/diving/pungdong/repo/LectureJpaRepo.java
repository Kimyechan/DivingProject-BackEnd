package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureJpaRepo extends JpaRepository<Lecture, Long> {
    Page<Lecture> findByRegion(String region, Pageable pageable);
}
