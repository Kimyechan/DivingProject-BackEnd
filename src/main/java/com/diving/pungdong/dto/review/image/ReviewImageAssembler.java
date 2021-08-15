package com.diving.pungdong.dto.review.image;

import com.diving.pungdong.domain.review.ReviewImage;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class ReviewImageAssembler implements RepresentationModelAssembler<ReviewImage, ReviewImageModel> {
    @Override
    public ReviewImageModel toModel(ReviewImage entity) {
        return new ReviewImageModel(entity);
    }

    @Override
    public CollectionModel<ReviewImageModel> toCollectionModel(Iterable<? extends ReviewImage> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}