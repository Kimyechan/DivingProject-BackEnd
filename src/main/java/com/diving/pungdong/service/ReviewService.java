package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.review.ReviewImage;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.dto.review.create.ReviewCreateInfo;
import com.diving.pungdong.repo.ReviewJpaRepo;
import com.diving.pungdong.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewJpaRepo reviewJpaRepo;
    private final LectureService lectureService;
    private final ReservationService reservationService;

    @Transactional(readOnly = true)
    public Review findByReviewId(Long reviewId) {
        return reviewJpaRepo.findById(reviewId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<Review> findByLectureAndSortCondition(Long lectureId, Pageable pageable) {
        Lecture lecture = lectureService.findLectureById(lectureId);

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

        return new PageImpl<>(reviewInfos, reviewPage.getPageable(), reviewPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<String> mapToReviewImageUrls(List<ReviewImage> reviewImages) {
        List<String> reviewImageUrls = new ArrayList<>();
        for (ReviewImage reviewImage : reviewImages) {
            reviewImageUrls.add(reviewImage.getUrl());
        }

        return reviewImageUrls;
    }

    @Transactional
    public Review saveReviewInfo(Account account, ReviewCreateInfo reviewCreateInfo) {
        Reservation reservation = reservationService.findById(reviewCreateInfo.getReservationId());
        checkPossibleDate(reservation.getSchedule().getScheduleDateTimes());
        checkPossibleReviewer(reservation.getAccount(), account);

        Lecture lecture = reservation.getSchedule().getLecture();
        Review review = Review.builder()
                .instructorStar(reviewCreateInfo.getInstructorStar())
                .lectureStar(reviewCreateInfo.getLectureStar())
                .locationStar(reviewCreateInfo.getLocationStar())
                .description(reviewCreateInfo.getDescription())
                .writeDate(LocalDate.now())
                .writer(account)
                .lecture(lecture)
                .build();

        Review savedReview = reviewJpaRepo.save(review);
        lecture.setReviewTotalAvg(savedReview.getTotalStarAvg());
        reservation.setReview(savedReview);

        return savedReview;
    }

    public void checkPossibleReviewer(Account reservationOwner, Account account) {
        if (!reservationOwner.getId().equals(account.getId())) {
            throw new NoPermissionsException();
        }
    }

    @Transactional(readOnly = true)
    public void checkPossibleDate(List<ScheduleDateTime> scheduleDateTimes) {
        scheduleDateTimes.sort(Comparator.comparing(ScheduleDateTime::getDate).reversed());

        ScheduleDateTime lastDateTime = scheduleDateTimes.get(0);
        if (lastDateTime.getDate().isAfter(LocalDate.now()) ||
                (lastDateTime.getDate().isEqual(LocalDate.now()) && lastDateTime.getEndTime().isAfter(LocalTime.now()))) {
            throw new BadRequestException("지금은 리뷰를 작성하지 못합니다");
        }
    }
}
