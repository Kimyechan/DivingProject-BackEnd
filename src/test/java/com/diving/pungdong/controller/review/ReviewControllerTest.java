package com.diving.pungdong.controller.review;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.dto.review.ReviewInfo;
import com.diving.pungdong.dto.review.create.ReviewCreateInfo;
import com.diving.pungdong.dto.review.image.create.ReviewImageInfo;
import com.diving.pungdong.service.ReviewService;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.review.ReviewImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
@ActiveProfiles("test")
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ReviewImageService reviewImageService;

    @MockBean
    private AccountService accountService;

    public Account createAccount(Role role) {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(role))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new UserAccount(account));

        return account;
    }

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
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "review-find-list",
                                requestParameters(
                                        parameterWithName("lectureId").description("강의 식별자 값"),
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기"),
                                        parameterWithName("sort").description("정렬 기준")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.reviewInfoList[].id").description("리뷰 식별자 값"),
                                        fieldWithPath("_embedded.reviewInfoList[].instructorStar").description("강사 평점"),
                                        fieldWithPath("_embedded.reviewInfoList[].lectureStar").description("강의 평점"),
                                        fieldWithPath("_embedded.reviewInfoList[].locationStar").description("강의 장소 평점"),
                                        fieldWithPath("_embedded.reviewInfoList[].totalStarAvg").description("강의 총 평점"),
                                        fieldWithPath("_embedded.reviewInfoList[].description").description("리뷰 내용"),
                                        fieldWithPath("_embedded.reviewInfoList[].writeDate").description("리뷰 작성 날짜"),
                                        fieldWithPath("_embedded.reviewInfoList[].reviewImageUrls[]").description("리뷰 이미지 Url들"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("page.size").description("한 페이지 당 사이즈"),
                                        fieldWithPath("page.totalElements").description("전체 신규 강의 갯수"),
                                        fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                        fieldWithPath("page.number").description("현재 페이지 번호")
                                )
                        )
                );
    }

    @Test
    @DisplayName("리뷰 정보 작성하기")
    public void createReviewInfo() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        ReviewCreateInfo reviewCreateInfo = ReviewCreateInfo.builder()
                .reservationId(1L)
                .instructorStar(4.5f)
                .lectureStar(4.5f)
                .locationStar(4.5f)
                .description("잘 가르쳐 주십니다")
                .build();

        given(reviewService.saveReviewInfo(any(), any())).willReturn(Review.builder().id(1L).build());

        mockMvc.perform(post("/review")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(reviewCreateInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("review-create",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("reservationId").description("예약 식별자 값"),
                                        fieldWithPath("instructorStar").description("강사 평점"),
                                        fieldWithPath("lectureStar").description("강의 평점"),
                                        fieldWithPath("locationStar").description("장소 평점"),
                                        fieldWithPath("description").description("평가 상세 내용")
                                ),
                                responseFields(
                                        fieldWithPath("reviewId").description("리뷰 식별자 값"),
                                        fieldWithPath("_links.self.href").description("해당 API URL")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 리뷰 이미지 등록")
    public void createReviewImages() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        Long reviewId = 1L;
        Long reservationId = 1L;

        MockMultipartFile file1 = new MockMultipartFile("reviewImages", "test1", "image/png", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("reviewImages", "test2", "image/png", "test data".getBytes());

        List<ReviewImageInfo> reviewImageInfos = new ArrayList<>();
        ReviewImageInfo reviewImageInfo = ReviewImageInfo.builder()
                .reviewImageId(1L)
                .imageUrl("url 1")
                .build();
        reviewImageInfos.add(reviewImageInfo);

        given(reviewImageService.saveReviewImages(any(), any(), any(), any())).willReturn(reviewImageInfos);

        mockMvc.perform(multipart("/review/image/list")
                .file(file1)
                .file(file2)
                .param("reservationId", String.valueOf(reservationId))
                .param("reviewId", String.valueOf(reviewId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "review-images-create",
                                requestHeaders(
                                        headerWithName(org.apache.http.HttpHeaders.CONTENT_TYPE).description("multipart form data 타입"),
                                        headerWithName(org.apache.http.HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("reviewId").description("리뷰 식별자 값"),
                                        parameterWithName("reservationId").description("예약 식별자 값")
                                ),
                                requestParts(
                                        partWithName("reviewImages").description("리뷰 이미지들")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.reviewImageInfoList[].reviewImageId").description("강의 이미지 식별자 값"),
                                        fieldWithPath("_embedded.reviewImageInfoList[].imageUrl").description("강의 이미지 Url"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url")
                                )
                        )
                );
    }
}