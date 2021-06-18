package com.diving.pungdong.repo;

import com.diving.pungdong.domain.review.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageJpaRepo extends JpaRepository<ReviewImage, Long> {
}
