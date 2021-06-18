package com.diving.pungdong.controller.review;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.review.ReviewImage;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.dto.review.create.ReviewCreateInfo;
import com.diving.pungdong.dto.review.create.ReviewCreateResult;
import com.diving.pungdong.dto.review.image.create.ReviewImageInfo;
import com.diving.pungdong.service.ReviewService;
import com.diving.pungdong.service.review.ReviewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;

    @GetMapping("/list")
    public ResponseEntity<?> findLectureReviewsByCondition(@NotNull @RequestParam Long lectureId,
                                                           Pageable pageable,
                                                           PagedResourcesAssembler<ReviewInfo> assembler) {
        Page<Review> reviewPage = reviewService.findByLectureAndSortCondition(lectureId, pageable);
        Page<ReviewInfo> reviewInfoPage = reviewService.mapToReviewInfos(reviewPage);

        PagedModel<EntityModel<ReviewInfo>> model = assembler.toModel(reviewInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @PostMapping
    public ResponseEntity<?> createReviewInfo(@CurrentUser Account account,
                                              @Valid @RequestBody ReviewCreateInfo reviewCreateInfo,
                                              BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        Review review = reviewService.saveReviewInfo(account, reviewCreateInfo);

        ReviewCreateResult reviewCreateResult = new ReviewCreateResult(review.getId());
        EntityModel<ReviewCreateResult> model = EntityModel.of(reviewCreateResult);
        WebMvcLinkBuilder linkBuilder = linkTo(methodOn(ReviewController.class).createReviewInfo(account, reviewCreateInfo, result));
        model.add(linkBuilder.withSelfRel());

        return ResponseEntity.created(linkBuilder.toUri()).body(model);
    }

    @PostMapping("/image/list")
    public ResponseEntity<?> createReviewImages(@CurrentUser Account account,
                                                @RequestParam Long reservationId,
                                                @RequestParam Long reviewId,
                                                @RequestParam List<MultipartFile> reviewImages) throws IOException {
        List<ReviewImageInfo> reviewImageInfos = reviewImageService.saveReviewImages(account, reservationId, reviewId, reviewImages);

        CollectionModel<ReviewImageInfo> model = CollectionModel.of(reviewImageInfos);
        WebMvcLinkBuilder selfLink = linkTo(methodOn(ReviewController.class).createReviewImages(account,reservationId, reviewId, reviewImages));
        model.add(selfLink.withSelfRel());

        return ResponseEntity.created(selfLink.toUri()).body(model);
    }
}
