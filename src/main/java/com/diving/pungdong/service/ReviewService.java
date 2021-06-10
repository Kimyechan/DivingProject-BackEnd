package com.diving.pungdong.service;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.review.ReviewImage;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.repo.ReviewJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewJpaRepo reviewJpaRepo;
    private final LectureService lectureService;

    @Transactional(readOnly = true)
    public Page<Review> findByLectureAndSortCondition(Long lectureId, Pageable pageable) {
        Lecture lecture = lectureService.getLectureById(lectureId);

        return reviewJpaRepo.findByLecture(lecture, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReviewInfo> mapToReviewInfos(Page<Review> reviewPage) {
        List<ReviewInfo> reviewInfos = new ArrayList<>();
        for (Review review : reviewPage.getContent()) {
            List<String> reviewImageUrls = mapToReviewImageUrls(review.getReviewImages());
            ReviewInfo reviewInfo = ReviewInfo.builder()
                    .id(review.getId())
                    .instructorStar(review.getInstructorStar())
                    .lectureStar(review.getLectureStar())
                    .locationStar(review.getLocationStar())
                    .totalStarAvg(review.getTotalStarAvg())
                    .description(review.getDescription())
                    .reviewImageUrls(reviewImageUrls)
                    .build();

            reviewInfos.add(reviewInfo);
        }

        return new PageImpl<>(reviewInfos,reviewPage.getPageable(), reviewPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<String> mapToReviewImageUrls(List<ReviewImage> reviewImages) {
        List<String> reviewImageUrls = new ArrayList<>();
        for (ReviewImage reviewImage : reviewImages) {
            reviewImageUrls.add(reviewImage.getUrl());
        }

        return reviewImageUrls;
    }
}
