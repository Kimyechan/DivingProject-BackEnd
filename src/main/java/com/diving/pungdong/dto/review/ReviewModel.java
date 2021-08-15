package com.diving.pungdong.dto.review;

import com.diving.pungdong.domain.review.Review;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Getter
public class ReviewModel extends RepresentationModel<ReviewModel> {
    private final Long id;
    private final Float instructorStar;
    private final Float lectureStar;
    private final Float locationStar;
    private final Float totalStarAvg;
    private final String description;
    private final LocalDate writeDate;

    public ReviewModel(Review review) {
        this.id = review.getId();
        this.instructorStar = review.getInstructorStar();
        this.lectureStar = review.getLectureStar();
        this.locationStar = review.getLocationStar();
        this.totalStarAvg = review.getTotalStarAvg();
        this.description = review.getDescription();
        this.writeDate = review.getWriteDate();
    }
}
