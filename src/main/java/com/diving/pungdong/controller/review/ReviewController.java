package com.diving.pungdong.controller.review;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.dto.review.create.ReviewCreateInfo;
import com.diving.pungdong.dto.review.create.ReviewCreateResult;
import com.diving.pungdong.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
public class ReviewController {
    private final ReviewService reviewService;

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
}
