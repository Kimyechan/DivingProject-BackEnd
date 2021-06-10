package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepo extends JpaRepository<Review, Long> {
    Page<Review> findByLecture(Lecture lecture, Pageable pageable);
}
