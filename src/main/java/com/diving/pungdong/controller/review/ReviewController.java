package com.diving.pungdong.controller.review;

import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/list")
    public ResponseEntity<?> findLectureReviewsByCondition(@Valid @RequestParam Long lectureId,
                                                           Pageable pageable,
                                                           PagedResourcesAssembler<ReviewInfo> assembler) {
        Page<Review> reviewPage = reviewService.findByLectureAndSortCondition(lectureId, pageable);
        Page<ReviewInfo> reviewInfoPage = reviewService.mapToReviewInfos(reviewPage);

        PagedModel<EntityModel<ReviewInfo>> model = assembler.toModel(reviewInfoPage);
        return ResponseEntity.ok().body(model);
    }


}
