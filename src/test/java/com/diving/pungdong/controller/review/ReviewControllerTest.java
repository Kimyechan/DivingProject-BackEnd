package com.diving.pungdong.controller.review;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
@ActiveProfiles("test")
class ReviewControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("한 강의의 리뷰 목록 정렬 조건 변경하여 조회")
    public void findLectureReviewsByCondition() throws Exception {
        Long lectureId = 1L;

        List<ReviewInfo> reviewInfos = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            ReviewInfo reviewInfo = ReviewInfo.builder()
                    .id((long) i)
                    .locationStar(4.5f)
                    .lectureStar(4.5f)
                    .instructorStar(4.5f)
                    .totalStarAvg(4.5f)
                    .description("잘 가르치시네요")
                    .reviewImageUrls(List.of("리뷰 이미지 URL 1", "리뷰 이미지 URL 2"))
                    .build();

            reviewInfos.add(reviewInfo);
        }

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "totalStarAvg"));
        Page<ReviewInfo> reviewPage = new PageImpl<>(reviewInfos, pageable, reviewInfos.size());

        given(reviewService.findByLectureAndSortCondition(any(), any())).willReturn(Page.empty());
        given(reviewService.mapToReviewInfos(any())).willReturn(reviewPage);

        mockMvc.perform(get("/review/list")
                .param("lectureId", String.valueOf(lectureId))
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .param("sort", String.valueOf(pageable.getSort())))
                .andDo(print())
                .andExpect(status().isOk());
    }

}