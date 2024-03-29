package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.advice.ExceptionAdvice;
import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.dto.lecture.LectureCreatorInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.detail.LectureDetail;
import com.diving.pungdong.dto.lecture.like.list.LikeLectureInfo;
import com.diving.pungdong.dto.lecture.like.mark.MarkLectureInfo;
import com.diving.pungdong.dto.lecture.like.mark.MarkLectureResult;
import com.diving.pungdong.dto.lecture.like.unmark.UnmarkLectureInfo;
import com.diving.pungdong.dto.lecture.list.LectureInfo;
import com.diving.pungdong.dto.lecture.list.mylist.MyLectureInfo;
import com.diving.pungdong.dto.lecture.list.newList.NewLectureInfo;
import com.diving.pungdong.dto.lecture.list.search.CostCondition;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
import com.diving.pungdong.dto.lecture.update.LectureClosedInfo;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.service.LectureMarkService;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.LectureImageService;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
import com.diving.pungdong.service.image.S3Uploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class, ExceptionAdvice.class})
class LectureControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    ModelMapper modelMapper;
    @MockBean
    AccountService accountService;
    @MockBean
    LectureService lectureService;
    @MockBean
    LectureImageService lectureImageService;
    @MockBean
    LectureEsService lectureEsService;
    @MockBean
    S3Uploader s3Uploader;
    @MockBean
    private LectureMarkService lectureMarkService;

    public Account createAccount() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new UserAccount(account));

        return account;
    }

    @Test
    @DisplayName("강사 자신의 강의 목록 조회")
    public void getLectureList() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        Pageable pageable = PageRequest.of(0, 5);
        Page<MyLectureInfo> myLectureInfos = createMyLectureInfoPage(pageable);

        given(lectureService.findMyLectureInfoList(account, pageable)).willReturn(myLectureInfos);

        mockMvc.perform(get("/lecture/manage/list")
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("lecture-find-my-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestParameters(
                                parameterWithName("page").description("몇 번째 페이지"),
                                parameterWithName("size").description("한 페이지당 크기")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.myLectureInfoList[].id").description("자신의 강의 식별자 값"),
                                fieldWithPath("_embedded.myLectureInfoList[].title").description("자신의 강의 제목"),
                                fieldWithPath("_embedded.myLectureInfoList[].organization").description("자신의 강의 자격증 단체 이"),
                                fieldWithPath("_embedded.myLectureInfoList[].level").description("자신의 강의 자격증 레벨"),
                                fieldWithPath("_embedded.myLectureInfoList[].region").description("자신의 강의 지역명"),
                                fieldWithPath("_embedded.myLectureInfoList[].maxNumber").description("강의 수강생 최대 인원 수"),
                                fieldWithPath("_embedded.myLectureInfoList[].period").description("자신의 강의 기간"),
                                fieldWithPath("_embedded.myLectureInfoList[].lectureTime").description("자신의 강의 총 강의 시간"),
                                fieldWithPath("_embedded.myLectureInfoList[].price").description("자신의 강의 강의 비용"),
                                fieldWithPath("_embedded.myLectureInfoList[].imageUrl").description("자신의 강의 대표 이미지"),
                                fieldWithPath("_embedded.myLectureInfoList[].equipmentNames[]").description("자신의 강의 대여 장비 목록"),
                                fieldWithPath("_embedded.myLectureInfoList[].leftScheduleDate").description("자신의 강의의 최신 일정 남은 날짜"),
                                fieldWithPath("_embedded.myLectureInfoList[].isClosed").description("강의 닫힘 여부"),
                                fieldWithPath("_links.self.href").description("해당 Api Url"),
                                fieldWithPath("page.size").description("한 페이지 당 사이즈"),
                                fieldWithPath("page.totalElements").description("전체 신규 강의 갯수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                fieldWithPath("page.number").description("현재 페이지 번호")
                        )
                ));
    }

    private Page<MyLectureInfo> createMyLectureInfoPage(Pageable pageable) {
        List<MyLectureInfo> myLectureInfos = new ArrayList<>();

        MyLectureInfo myLectureInfo = MyLectureInfo.builder()
                .id(1L)
                .title("title")
                .organization(Organization.AIDA)
                .level("Level1")
                .region("Seoul")
                .maxNumber(5)
                .period(5)
                .lectureTime(LocalTime.of(1, 30))
                .imageUrl("Url")
                .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                .leftScheduleDate(3L)
                .isClosed(false)
                .build();

        myLectureInfos.add(myLectureInfo);

        return new PageImpl<>(myLectureInfos, pageable, myLectureInfos.size());
    }

    @Test
    @DisplayName("강의 개설하기 실패 - 정보 기입 누락")
    public void createLectureBadRequest() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LectureCreateInfo lectureCreateInfo = LectureCreateInfo.builder()
                .title("프리 다이빙 강의")
                .classKind("프리 다이빙")
                .organization(Organization.AIDA)
                .level("Level1")
                .description("프리 다이빙 Level1 자격증을 쉽게 가져가세요")
                .price(100000)
                .maxNumber(5)
                .period(3)
                .region("서울")
                .build();

        mockMvc.perform(post("/lecture/create")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(lectureCreateInfo)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("강의 개설하기")
    public void createLecture() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LectureCreateInfo lectureCreateInfo = LectureCreateInfo.builder()
                .title("프리 다이빙 강의")
                .classKind("프리 다이빙")
                .organization(Organization.AIDA)
                .level("Level1")
                .description("프리 다이빙 Level1 자격증을 쉽게 가져가세요")
                .price(100000)
                .maxNumber(5)
                .period(3)
                .region("서울")
                .lectureTime(LocalTime.of(2, 30))
                .serviceTags(Set.of("주차 가능", "장비 대여 가능"))
                .build();

        LectureCreateResult result = LectureCreateResult.builder()
                .lectureId(1L)
                .build();

        given(lectureService.createLecture(account, lectureCreateInfo)).willReturn(result);

        mockMvc.perform(post("/lecture/create")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(lectureCreateInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "lecture-create",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("title").description("강의 제목"),
                                        fieldWithPath("region").description("강의 지역"),
                                        fieldWithPath("classKind").description("강의 종류"),
                                        fieldWithPath("organization").description("자격증 단체"),
                                        fieldWithPath("level").description("강의 자격증 레벨"),
                                        fieldWithPath("description").description("강의 설명"),
                                        fieldWithPath("price").description("강의 비용"),
                                        fieldWithPath("maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("period").description("강의 기간"),
                                        fieldWithPath("lectureTime").description("강의 총 소요 시간"),
                                        fieldWithPath("serviceTags[]").description("제공되는 서비스 목록")
                                ),
                                responseFields(
                                        fieldWithPath("lectureId").description("생성된 강의 식별자 값"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 정보 수정")
    public void updateLecture() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LectureUpdateInfo lectureUpdateInfo = LectureUpdateInfo.builder()
                .id(1L)
                .title("프리 다이빙 강의")
                .classKind("프리 다이빙")
                .organization(Organization.AIDA)
                .level("Level1")
                .description("프리 다이빙 Level1 자격증을 쉽게 가져가세요")
                .price(100000)
                .maxNumber(5)
                .period(3)
                .region("서울")
                .lectureTime(LocalTime.of(2, 30))
                .serviceTags(Set.of("주차 가능", "장비 대여 가능"))
                .build();

        mockMvc.perform(put("/lecture")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(lectureUpdateInfo)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "lecture-update",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("id").description("강의 식별자 값"),
                                        fieldWithPath("title").description("강의 제목"),
                                        fieldWithPath("region").description("강의 지역"),
                                        fieldWithPath("classKind").description("강의 종류"),
                                        fieldWithPath("organization").description("자격증 단체"),
                                        fieldWithPath("level").description("강의 자격증 레벨"),
                                        fieldWithPath("description").description("강의 설명"),
                                        fieldWithPath("price").description("강의 비용"),
                                        fieldWithPath("maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("period").description("강의 기간"),
                                        fieldWithPath("lectureTime").description("강의 총 소요 시간"),
                                        fieldWithPath("serviceTags[]").description("제공되는 서비스 목록")
                                )
                        )
                );
    }

    @Test
    @DisplayName("신규 강의 목록 조회")
    public void getNewLectures() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        Pageable pageable = PageRequest.of(0, 2);

        List<NewLectureInfo> lectureInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            NewLectureInfo newLectureInfo = NewLectureInfo.builder()
                    .id((long) i)
                    .title("title" + i)
                    .organization(Organization.AIDA)
                    .level("Level1")
                    .region("Seoul")
                    .maxNumber(5)
                    .period(5)
                    .lectureTime(LocalTime.of(1, 30))
                    .imageUrl("Url" + i)
                    .isMarked(false)
                    .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                    .build();
            lectureInfos.add(newLectureInfo);
        }
        Page<NewLectureInfo> newLectureInfoPage = new PageImpl<>(lectureInfos, pageable, lectureInfos.size());

        given(lectureService.getNewLecturesInfo(any(), any())).willReturn(newLectureInfoPage);

        mockMvc.perform(
                get("/lecture/new/list")
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize()))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .header("IsRefreshToken", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-get-new-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.newLectureInfoList[].id").description("신규 강의 식별자 값"),
                                        fieldWithPath("_embedded.newLectureInfoList[].title").description("신규 강의 제목"),
                                        fieldWithPath("_embedded.newLectureInfoList[].organization").description("신규 강의 자격증 단체 이"),
                                        fieldWithPath("_embedded.newLectureInfoList[].level").description("신규 강의 자격증 레벨"),
                                        fieldWithPath("_embedded.newLectureInfoList[].region").description("신규 강의 지역명"),
                                        fieldWithPath("_embedded.newLectureInfoList[].maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("_embedded.newLectureInfoList[].period").description("신규 강의 기간"),
                                        fieldWithPath("_embedded.newLectureInfoList[].lectureTime").description("신규 강의 총 강의 시간"),
                                        fieldWithPath("_embedded.newLectureInfoList[].isMarked").description("신규 강의 찜 여부"),
                                        fieldWithPath("_embedded.newLectureInfoList[].price").description("신규 강의 강의 비용"),
                                        fieldWithPath("_embedded.newLectureInfoList[].imageUrl").description("신규 강의 대표 이미지"),
                                        fieldWithPath("_embedded.newLectureInfoList[].equipmentNames[]").description("신규 강의 대여 장비 목록"),
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
    @DisplayName("인기 강의 목록 조회")
    public void getPopularLectures() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        Pageable pageable = PageRequest.of(0, 2);

        List<LectureInfo> lectureInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            LectureInfo lectureInfo = LectureInfo.builder()
                    .id((long) i)
                    .title("title" + i)
                    .organization(Organization.AIDA)
                    .level("Level1")
                    .region("Seoul")
                    .maxNumber(5)
                    .period(5)
                    .lectureTime(LocalTime.of(1, 30))
                    .imageUrl("Url" + i)
                    .isMarked(false)
                    .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                    .starAvg(4.5f)
                    .reviewCount(100)
                    .build();
            lectureInfos.add(lectureInfo);
        }
        Page<LectureInfo> lectureInfoPage = new PageImpl<>(lectureInfos, pageable, lectureInfos.size());

        given(lectureService.getPopularLecturesInfo(account, pageable)).willReturn(lectureInfoPage);

        mockMvc.perform(get("/lecture/popular/list")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-get-popular-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.lectureInfoList[].id").description("인기 강의 식별자 값"),
                                        fieldWithPath("_embedded.lectureInfoList[].title").description("인기 강의 제목"),
                                        fieldWithPath("_embedded.lectureInfoList[].organization").description("인기 강의 자격증 단체 이"),
                                        fieldWithPath("_embedded.lectureInfoList[].level").description("인기 강의 자격증 레벨"),
                                        fieldWithPath("_embedded.lectureInfoList[].region").description("인기 강의 지역명"),
                                        fieldWithPath("_embedded.lectureInfoList[].maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("_embedded.lectureInfoList[].period").description("인기 강의 기간"),
                                        fieldWithPath("_embedded.lectureInfoList[].lectureTime").description("인기 강의 총 강의 시간"),
                                        fieldWithPath("_embedded.lectureInfoList[].isMarked").description("인기 강의 찜 여부"),
                                        fieldWithPath("_embedded.lectureInfoList[].price").description("인기 강의 강의 비용"),
                                        fieldWithPath("_embedded.lectureInfoList[].imageUrl").description("인기 강의 대표 이미지"),
                                        fieldWithPath("_embedded.lectureInfoList[].equipmentNames[]").description("인기 강의 대여 장비 목록"),
                                        fieldWithPath("_embedded.lectureInfoList[].starAvg").description("인기 강의 리뷰 종합 평점"),
                                        fieldWithPath("_embedded.lectureInfoList[].reviewCount").description("인기 강의 리뷰 갯수"),
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
    @DisplayName("강의 필터 검색")
    public void searchListByFilter() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        Pageable pageable = PageRequest.of(0, 2);
        CostCondition costCondition = CostCondition.builder()
                .max(150000)
                .min(100000)
                .build();
        FilterSearchCondition condition = FilterSearchCondition.builder()
                .region("서울")
                .classKind("프리 다이빙")
                .organization(Organization.AIDA)
                .level("Level3")
                .costCondition(costCondition)
                .build();

        List<LectureInfo> lectureInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            LectureInfo lectureInfo = LectureInfo.builder()
                    .id((long) i)
                    .title("title" + i)
                    .organization(Organization.AIDA)
                    .level("Level1")
                    .region("Seoul")
                    .maxNumber(5)
                    .period(5)
                    .lectureTime(LocalTime.of(1, 30))
                    .imageUrl("Url" + i)
                    .isMarked(false)
                    .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                    .starAvg(4.5f)
                    .reviewCount(100)
                    .build();
            lectureInfos.add(lectureInfo);
        }
        Page<LectureInfo> lectureInfoPage = new PageImpl<>(lectureInfos, pageable, lectureInfos.size());

        given(lectureService.filterSearchList(account, condition, pageable)).willReturn(lectureInfoPage);

        mockMvc.perform(post("/lecture/list/search/filter")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .content(objectMapper.writeValueAsString(condition)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-search-filter-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기")
                                ),
                                requestFields(
                                        fieldWithPath("organization").description("자격증 단체 조건"),
                                        fieldWithPath("level").description("자격증 레벨 조건"),
                                        fieldWithPath("region").description("강의 지역 조건"),
                                        fieldWithPath("classKind").description("강의 종류 조건"),
                                        fieldWithPath("costCondition.max").description("강의료 최대 바용"),
                                        fieldWithPath("costCondition.min").description("강의료 최저 바용")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.lectureInfoList[].id").description("인기 강의 식별자 값"),
                                        fieldWithPath("_embedded.lectureInfoList[].title").description("인기 강의 제목"),
                                        fieldWithPath("_embedded.lectureInfoList[].organization").description("인기 강의 자격증 단체 이"),
                                        fieldWithPath("_embedded.lectureInfoList[].level").description("인기 강의 자격증 레벨"),
                                        fieldWithPath("_embedded.lectureInfoList[].region").description("인기 강의 지역명"),
                                        fieldWithPath("_embedded.lectureInfoList[].maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("_embedded.lectureInfoList[].period").description("인기 강의 기간"),
                                        fieldWithPath("_embedded.lectureInfoList[].lectureTime").description("인기 강의 총 강의 시간"),
                                        fieldWithPath("_embedded.lectureInfoList[].isMarked").description("인기 강의 찜 여부"),
                                        fieldWithPath("_embedded.lectureInfoList[].price").description("인기 강의 강의 비용"),
                                        fieldWithPath("_embedded.lectureInfoList[].imageUrl").description("인기 강의 대표 이미지"),
                                        fieldWithPath("_embedded.lectureInfoList[].equipmentNames[]").description("인기 강의 대여 장비 목록"),
                                        fieldWithPath("_embedded.lectureInfoList[].starAvg").description("인기 강의 리뷰 종합 평점"),
                                        fieldWithPath("_embedded.lectureInfoList[].reviewCount").description("인기 강의 리뷰 갯수"),
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
    @DisplayName("강의 키워드 검색")
    public void searchListByKeyword() throws Exception {
        String keyword = "프리 다이빙";

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        Pageable pageable = PageRequest.of(0, 2);

        List<LectureInfo> lectureInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            LectureInfo lectureInfo = LectureInfo.builder()
                    .id((long) i)
                    .title("프리 다이빙")
                    .organization(Organization.AIDA)
                    .level("Level1")
                    .region("Seoul")
                    .maxNumber(5)
                    .period(5)
                    .lectureTime(LocalTime.of(1, 30))
                    .imageUrl("Url" + i)
                    .price(100000)
                    .isMarked(false)
                    .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                    .starAvg(4.5f)
                    .reviewCount(100)
                    .build();
            lectureInfos.add(lectureInfo);
        }
        Page<LectureInfo> lectureInfoPage = new PageImpl<>(lectureInfos, pageable, lectureInfos.size());

        given(lectureEsService.getListContainKeyword(any(), any(), any())).willReturn(lectureInfoPage);

        mockMvc.perform(get("/lecture/list/search/keyword")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-search-keyword-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("keyword").description("검색 키워드"),
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.lectureInfoList[].id").description("인기 강의 식별자 값"),
                                        fieldWithPath("_embedded.lectureInfoList[].title").description("인기 강의 제목"),
                                        fieldWithPath("_embedded.lectureInfoList[].organization").description("인기 강의 자격증 단체 이"),
                                        fieldWithPath("_embedded.lectureInfoList[].level").description("인기 강의 자격증 레벨"),
                                        fieldWithPath("_embedded.lectureInfoList[].region").description("인기 강의 지역명"),
                                        fieldWithPath("_embedded.lectureInfoList[].maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("_embedded.lectureInfoList[].period").description("인기 강의 기간"),
                                        fieldWithPath("_embedded.lectureInfoList[].lectureTime").description("인기 강의 총 강의 시간"),
                                        fieldWithPath("_embedded.lectureInfoList[].isMarked").description("인기 강의 찜 여부"),
                                        fieldWithPath("_embedded.lectureInfoList[].price").description("인기 강의 강의 비용"),
                                        fieldWithPath("_embedded.lectureInfoList[].imageUrl").description("인기 강의 대표 이미지"),
                                        fieldWithPath("_embedded.lectureInfoList[].equipmentNames[]").description("인기 강의 대여 장비 목록"),
                                        fieldWithPath("_embedded.lectureInfoList[].starAvg").description("인기 강의 리뷰 종합 평점"),
                                        fieldWithPath("_embedded.lectureInfoList[].reviewCount").description("인기 강의 리뷰 갯수"),
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
    @DisplayName("해당 강의를 개설한 강사 정보 조회")
    public void findInstructorInfoForLecture() throws Exception {
        Long lectureId = 1L;

        LectureCreatorInfo lectureCreatorInfo = LectureCreatorInfo.builder()
                .instructorId(2L)
                .nickName("열혈 다이버")
                .selfIntroduction("안녕하세요 열혈 다이버입니다")
                .profilePhotoUrl("강사 프로필 사진 Url")
                .build();

        given(lectureService.findLectureCreatorInfo(lectureId)).willReturn(lectureCreatorInfo);

        mockMvc.perform(get("/lecture/instructor/info/creator")
                .param("lectureId", String.valueOf(lectureId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-find-instructor-info",
                                requestParameters(
                                        parameterWithName("lectureId").description("강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("instructorId").description("강사 식별자 Id"),
                                        fieldWithPath("nickName").description("강사 닉네임"),
                                        fieldWithPath("selfIntroduction").description("강사 자기 소개"),
                                        fieldWithPath("profilePhotoUrl").description("강사 프로필 사진 URL"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("해당 강의 정보 조회")
    public void findLectureInfo() throws Exception {
        Long id = 1L;

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LectureDetail lectureDetail = LectureDetail.builder()
                .id(id)
                .title("강의 타이틀")
                .classKind("강의 종류")
                .organization(Organization.AIDA)
                .level("강의 자격증 레벨")
                .description("강의 설명")
                .maxNumber(5)
                .price(100000)
                .region("서울")
                .period(5)
                .reviewTotalAvg(4.5f)
                .reviewCount(100)
                .serviceTags(Set.of("주차 가능", "장비 대여 가능"))
                .isClosed(false)
                .build();

        given(lectureService.findLectureDetailInfo(any())).willReturn(lectureDetail);

        mockMvc.perform(get("/lecture")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("id", String.valueOf(id)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-find-info",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("id").description("강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("강의 식별자 값"),
                                        fieldWithPath("title").description("강의 타이틀"),
                                        fieldWithPath("classKind").description("강의 종류"),
                                        fieldWithPath("organization").description("강의 관련 자격증 단체"),
                                        fieldWithPath("level").description("강의 자격증 종류"),
                                        fieldWithPath("description").description("강의 설명"),
                                        fieldWithPath("maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("period").description("강의 기간"),
                                        fieldWithPath("price").description("강의 1인당 가격"),
                                        fieldWithPath("region").description("강의 지역"),
                                        fieldWithPath("reviewTotalAvg").description("강의 리뷰 전체 평균"),
                                        fieldWithPath("reviewCount").description("강의 리뷰 갯수"),
                                        fieldWithPath("isClosed").description("강의 닫힘 여부"),
                                        fieldWithPath("serviceTags[]").description("제공되는 서비스 목록"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("한 강의의 찜 여부 조회")
    public void readLectureMark() throws Exception {
        Long lectureId = 1L;

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        given(lectureMarkService.existLectureMark(account, lectureId)).willReturn(true);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/lecture/{id}/like", lectureId)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("lecture-read-mark",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("id").optional()
                        ),
                        pathParameters(
                                parameterWithName("id").description("강의 식별자")
                        ),
                        responseFields(
                                fieldWithPath("marked").description("좋아요 여부"),
                                fieldWithPath("_links.self.href").description("해당 자원 조회 URL")
                        )
                ));
    }

    @Test
    @DisplayName("강의 찜하기")
    public void markLikeLecture() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        MarkLectureInfo markLectureInfo = MarkLectureInfo.builder()
                .lectureId(1L)
                .build();

        MarkLectureResult markLectureResult = MarkLectureResult.builder()
                .lectureMarkId(1L)
                .lectureId(1L)
                .accountId(1L)
                .build();

        given(lectureService.markLecture(any(), any())).willReturn(markLectureResult);

        mockMvc.perform(post("/lecture/like")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(markLectureInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-mark-like",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("lectureMarkId").description("강의 좋아요 식별자 값"),
                                        fieldWithPath("accountId").description("계정 식별자 값"),
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("찜한 강의 목록 조회")
    public void findLikeLectureList() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        Pageable pageable = PageRequest.of(0, 2);
        List<LikeLectureInfo> likeLectureInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            LikeLectureInfo likeLectureInfo = LikeLectureInfo.builder()
                    .id((long) i)
                    .title("프리 다이빙")
                    .organization(Organization.AIDA)
                    .level("Level1")
                    .region("Seoul")
                    .maxNumber(5)
                    .period(5)
                    .lectureTime(LocalTime.of(1, 30))
                    .imageUrl("Url" + i)
                    .price(100000)
                    .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                    .starAvg(4.5f)
                    .reviewCount(100)
                    .isMarked(true)
                    .build();
            likeLectureInfos.add(likeLectureInfo);
        }
        Page<LikeLectureInfo> likeLectureInfoPage = new PageImpl<>(likeLectureInfos, pageable, likeLectureInfos.size());

        given(lectureService.findLikeLectures(any(), any())).willReturn(Page.empty());
        given(lectureService.mapToLikeLectureInfos(any())).willReturn(likeLectureInfoPage);

        mockMvc.perform(get("/lecture/like/list")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-read-like-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.likeLectureInfoList[].id").description("강의 식별자 값"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].title").description("강의 제목"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].organization").description("강의 자격증 단체 이"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].level").description("강의 자격증 레벨"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].region").description("강의 지역명"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].maxNumber").description("강의 수강생 최대 인원 수"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].period").description("강의 기간"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].lectureTime").description(" 강의 총 강의 시간"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].price").description("강의 강의 비용"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].imageUrl").description(" 강의 대표 이미지"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].equipmentNames[]").description("강의 대여 장비 목록"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].starAvg").description("강의 리뷰 종합 평점"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].reviewCount").description("강의 리뷰 갯수"),
                                        fieldWithPath("_embedded.likeLectureInfoList[].isMarked").description("강의 찜 여부"),
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
    @DisplayName("강의 찜하기 취소")
    public void unmarkLikeLecture() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        UnmarkLectureInfo unmarkLectureInfo = UnmarkLectureInfo.builder()
                .lectureId(1L)
                .build();

        mockMvc.perform(delete("/lecture/unlike")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(unmarkLectureInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lecture-unmark-like",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 개시 및 중지")
    public void controlLectureClosed() throws Exception {
        Long lectureId = 1L;

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LectureClosedInfo info = LectureClosedInfo.builder()
                .isClosed(true)
                .build();

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/lecture/{id}/closed", lectureId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(info)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "lecture-update-closed",
                                pathParameters(
                                        parameterWithName("id").description("강의 식별자 값")
                                ),
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("isClosed").description("강의 닫기 여부")
                                )
                        )
                );
    }

    @Test
    @DisplayName("ElasticSearch에 강의 데이터 저장")
    public void createLectureEs() throws Exception {
        Long lectureId = 1L;

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        mockMvc.perform(RestDocumentationRequestBuilders.post("/lecture/{id}/elastic-search", lectureId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "lecture-create-elasticsearch",
                                pathParameters(
                                        parameterWithName("id").description("강의 식별자 값")
                                ),
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                )
                        )
                );
    }
}