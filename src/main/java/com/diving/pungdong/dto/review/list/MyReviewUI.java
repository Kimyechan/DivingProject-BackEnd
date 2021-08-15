package com.diving.pungdong.dto.review.list;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.review.ReviewImage;
import com.diving.pungdong.dto.lecture.LectureTitleUI;
import com.diving.pungdong.dto.review.ReviewModel;
import com.diving.pungdong.dto.review.image.ReviewImageModel;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Getter
public class MyReviewUI extends RepresentationModel<MyReviewUI> {
    private final LectureTitleUI lectureTitleUI;
    private final ReviewModel reviewModel;
    private final List<ReviewImageModel> reviewImageModels;

    public MyReviewUI(Lecture lecture, Review review, List<ReviewImage> reviewImages) {
        this.lectureTitleUI = new LectureTitleUI(lecture);
        this.reviewModel = new ReviewModel(review);
        this.reviewImageModels = ReviewImageModel.toList(reviewImages);
    }
}
