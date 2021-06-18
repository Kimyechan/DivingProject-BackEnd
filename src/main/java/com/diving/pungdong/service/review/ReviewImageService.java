package com.diving.pungdong.service.review;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.review.ReviewImage;
import com.diving.pungdong.dto.review.image.create.ReviewImageInfo;
import com.diving.pungdong.repo.ReviewImageJpaRepo;
import com.diving.pungdong.service.ReviewService;
import com.diving.pungdong.service.image.S3Uploader;
import com.diving.pungdong.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewImageService {
    private final ReviewImageJpaRepo reviewImageJpaRepo;
    private final S3Uploader s3Uploader;
    private final ReviewService reviewService;
    private final ReservationService reservationService;

    @Transactional
    public List<ReviewImageInfo> saveReviewImages(Account account, Long reservationId, Long reviewId, List<MultipartFile> images) throws IOException {
        Reservation reservation = reservationService.findById(reservationId);
        reviewService.checkPossibleReviewer(reservation.getAccount(), account);

        Review review = reviewService.findByReviewId(reviewId);

        List<ReviewImageInfo> reviewImageInfos = new ArrayList<>();

        for (MultipartFile image : images) {
            String fileUrl = s3Uploader.upload(image, "review-image", account.getEmail());
            ReviewImage reviewImage = ReviewImage.builder()
                    .url(fileUrl)
                    .review(review)
                    .build();
            ReviewImage savedReview = reviewImageJpaRepo.save(reviewImage);

            ReviewImageInfo reviewImageInfo = ReviewImageInfo.builder()
                    .reviewImageId(savedReview.getId())
                    .imageUrl(fileUrl)
                    .build();
            reviewImageInfos.add(reviewImageInfo);
        }

        return reviewImageInfos;
    }
}
