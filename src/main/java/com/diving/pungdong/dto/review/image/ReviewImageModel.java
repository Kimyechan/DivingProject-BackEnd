package com.diving.pungdong.dto.review.image;

import com.diving.pungdong.domain.review.ReviewImage;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ReviewImageModel extends RepresentationModel<ReviewImageModel> {
    private final Long id;
    private final String url;

    public ReviewImageModel(ReviewImage reviewImage) {
        this.id = reviewImage.getId();
        this.url = reviewImage.getUrl();
    }

    public static List<ReviewImageModel> toList(List<ReviewImage> reviewImages) {
        List<ReviewImageModel> reviewImageModels = new ArrayList<>();
        for (ReviewImage reviewImage : reviewImages) {
            ReviewImageModel reviewImageModel = new ReviewImageModel(reviewImage);

            reviewImageModels.add(reviewImageModel);
        }

        return reviewImageModels;
    }
}
