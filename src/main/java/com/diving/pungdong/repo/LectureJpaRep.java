package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureJpaRep extends JpaRepository<Lecture, Long> {
}
