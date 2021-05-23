package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.popularList.PopularLectureInfo;
import com.diving.pungdong.dto.lecture.newList.NewLectureInfo;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.LectureImageService;
import com.diving.pungdong.service.LectureService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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
@Import({RestDocsConfiguration.class})
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
    S3Uploader s3Uploader;

    public Account createAccount() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .userName("yechan")
                .age(27)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new UserAccount(account));

        return account;
    }
//
//    @Test
//    @DisplayName("강의 정보 수정")
//    public void update() throws Exception {
//        Account account = createAccount();
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
//
//        Lecture lecture = Lecture.builder()
//                .id(1L)
//                .title("강의1")
//                .classKind("스쿠버다이빙")
//                .organization(Organization.AIDA)
//                .level("Level1")
//                .description("강의 설명")
//                .price(300000)
//                .region("서울")
//                .instructor(account)
//                .lectureImages(List.of(LectureImage.builder().fileURI("File URL1").build()))
//                .equipmentList(List.of(Equipment.builder().name("장비1").price(3000).build()))
//                .build();
//
//        LectureUpdateInfo lectureUpdateInfo = LectureUpdateInfo.builder()
//                .id(1L)
//                .title("강의 제목 Update")
//                .classKind("스킨 스쿠버")
//                .organization(Organization.AIDA)
//                .level("Level2")
//                .description("강의 설명  Update")
//                .price(400000)
//                .period(5)
//                .studentCount(6)
//                .region("부산")
//                .lectureImageUpdateList(List.of(LectureImageUpdate.builder().lectureImageURL("File URL1").isDeleted(true).build()))
//                .equipmentUpdateList(List.of(EquipmentUpdate.builder().name("장비1").price(5000).isDeleted(false).build()))
//                .build();
//
//        Lecture updatedLecture = Lecture.builder()
//                .id(1L)
//                .title("강의 제목 Update")
//                .classKind("스킨 스쿠버")
//                .organization(Organization.AIDA)
//                .level("Level2")
//                .description("강의 설명  Update")
//                .price(400000)
//                .region("부산")
//                .instructor(Account.builder().id(10L).build())
//                .equipmentList(List.of(Equipment.builder().name("장비1").price(5000).build()))
//                .build();
//
//        MockMultipartFile file1 = new MockMultipartFile("fileList", "test1.txt", "image/png", "test data".getBytes());
//        MockMultipartFile file2 = new MockMultipartFile("fileList", "test2.txt", "image/png", "test data".getBytes());
//
//        MockMultipartFile request =
//                new MockMultipartFile("request",
//                        "request",
//                        MediaType.APPLICATION_JSON_VALUE,
//                        objectMapper.writeValueAsString(lectureUpdateInfo).getBytes());
//
//        given(lectureService.getLectureById(1L)).willReturn(lecture);
//        given(lectureService.updateLectureTx(eq(account.getEmail()), eq(lectureUpdateInfo), anyList(), eq(lecture))).willReturn(updatedLecture);
//
//        mockMvc.perform(multipart("/lecture/update")
//                .file(file1)
//                .file(file2)
//                .file(request)
//                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("update-lecture",
//                        requestHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("multipart form data 타입"),
//                                headerWithName("Authorization").description("access token 값"),
//                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
//                        ),
//                        requestParts(
//                                partWithName("request").description("강의 수정 정보 JSON 데이터"),
//                                partWithName("fileList").description("추가할 이미지 파일 리스트")
//                        ),
//                        requestPartBody("fileList"),
//                        requestPartBody("request"),
//                        requestPartFields(
//                                "request",
//                                fieldWithPath("id").description("강의 식별자"),
//                                fieldWithPath("title").description("강의 이름"),
//                                fieldWithPath("classKind").description("강의 종류"),
//                                fieldWithPath("groupName").description("단체명"),
//                                fieldWithPath("certificateKind").description("자격증 종류"),
//                                fieldWithPath("description").description("강의 설명"),
//                                fieldWithPath("price").description("강의 비용"),
//                                fieldWithPath("period").description("강의 기간"),
//                                fieldWithPath("studentCount").description("수강 인원 제한"),
//                                fieldWithPath("region").description("강의 지역"),
//                                fieldWithPath("lectureImageUpdateList[0].lectureImageURL").description("첫번째 강의 이미지 링크"),
//                                fieldWithPath("lectureImageUpdateList[0].isDeleted").description("해당 이미지 삭제 여부 체크"),
//                                fieldWithPath("equipmentUpdateList[0].name").description("첫번째 대여 장비 이름"),
//                                fieldWithPath("equipmentUpdateList[0].price").description("첫번째 대여 장비 가격"),
//                                fieldWithPath("equipmentUpdateList[0].isDeleted").description("해당 장비 정보 삭제 여부 체크")
//                        ),
//                        responseHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
//                        ),
//                        responseFields(
//                                fieldWithPath("id").description("강의 식별자"),
//                                fieldWithPath("title").description("강의 이름"),
//                                fieldWithPath("_links.self.href").description("해당 API 주소")
//                        )
//                ));
//    }
//
//    @Test
//    @DisplayName("강의 삭제")
//    public void deleteLecture() throws Exception {
//        Account account = createAccount();
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
//
//        Lecture lecture = Lecture.builder()
//                .id(1L)
//                .instructor(account)
//                .build();
//
//        given(lectureService.getLectureById(1L)).willReturn(lecture);
//
//        mockMvc.perform(delete("/lecture/delete")
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false")
//                .param("id", "1"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("lectureId").exists())
//                .andDo(document("delete-lecture",
//                        requestHeaders(
//                                headerWithName("Authorization").description("access token 값"),
//                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
//                        ),
//                        requestParameters(
//                                parameterWithName("id").description("삭제 요청할 강의 식별자 값")
//                        ),
//                        responseHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("\"HAL JSON 타입\"")
//                        ),
//                        responseFields(
//                                fieldWithPath("lectureId").description("삭제된 강의 식별자 값"),
//                                fieldWithPath("_links.self.href").description("해당 API URI")
//                        )
//                ));
//
//        verify(lectureService, times(1)).deleteLectureById(anyLong());
//    }
//
//    @Test
//    @DisplayName("이미지 파일 업로드")
//    public void upload() throws Exception {
//        Account account = createAccount();
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
//        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
//        given(s3Uploader.upload(file, "lecture", account.getEmail())).willReturn("image file aws s3 url");
//
//        mockMvc.perform(multipart("/lecture/upload")
//                .file(file)
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false"))
//                .andDo(print())
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("강의 상세 정보 조회")
//    public void getLectureDetail() throws Exception {
//        Account account = createAccount();
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
//
//        Lecture lecture = Lecture.builder()
//                .title("강의1")
//                .classKind("스쿠버다이빙")
//                .organization(Organization.AIDA)
//                .level("Level1")
//                .description("강의 설명")
//                .price(300000)
//                .region("서울")
//                .instructor(Account.builder().id(10L).build())
//                .lectureImages(List.of(LectureImage.builder().fileURI("File URL1").build()))
//                .equipmentList(List.of(Equipment.builder().name("장비1").price(3000).build()))
//                .build();
//
//        given(lectureService.getLectureById(1L)).willReturn(lecture);
//
//        mockMvc.perform(get("/lecture/detail")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false")
//                .param("id", "1"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("get-lecture-detail",
//                        requestHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
//                                headerWithName("Authorization").description("access token 값"),
//                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
//                        ),
//                        requestParameters(
//                                parameterWithName("id").description("lecture 식별자 값")
//                        ),
//                        responseHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
//                        ),
//                        responseFields(
//                                fieldWithPath("id").description("강의 식별자 ID"),
//                                fieldWithPath("title").description("강의 제목"),
//                                fieldWithPath("classKind").description("강의 분야"),
//                                fieldWithPath("groupName").description("강사 소속 그룹"),
//                                fieldWithPath("certificateKind").description("해당 강의 후 취득 자격증"),
//                                fieldWithPath("description").description("강의 설명"),
//                                fieldWithPath("price").description("강의 비용"),
//                                fieldWithPath("period").description("강의 기간"),
//                                fieldWithPath("studentCount").description("최대 제한 인원수"),
//                                fieldWithPath("region").description("강의 지역"),
//                                fieldWithPath("instructorId").description("강사 식별자 ID"),
//                                fieldWithPath("lectureUrlList[0]").description("강의 이미지 URL"),
//                                fieldWithPath("equipmentList[0].name").description("대여 장비 이름"),
//                                fieldWithPath("equipmentList[0].price").description("대여 장비 가격"),
//                                fieldWithPath("_links.self.href").description("해당 API URL")
//                        )
//
//                ));
//    }
//
//    @Test
//    @DisplayName("강의 리스트 조건 검색")
//    public void searchList() throws Exception {
//        CostCondition costCondition = CostCondition.builder()
//                .min(10000)
//                .max(50000)
//                .build();
//
//        SearchCondition searchCondition = SearchCondition.builder()
//                .groupName("AIDA")
//                .certificateKind("LEVEL1")
//                .region("서울")
//                .costCondition(costCondition)
//                .build();
//
//        Pageable pageable = PageRequest.of(1, 5);
//        Page<Lecture> lecturePage = createLecturePage(pageable);
//        given(lectureService.searchListByCondition(searchCondition, pageable)).willReturn(lecturePage);
//
//        mockMvc.perform(post("/lecture/list")
//                .param("page", String.valueOf(pageable.getPageNumber()))
//                .param("size", String.valueOf(pageable.getPageSize()))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(searchCondition)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("get-lecture-by-condition",
//                        requestParameters(
//                                parameterWithName("page").description("몇 번째 페이지"),
//                                parameterWithName("size").description("한 페이지당 크기")
//                        ),
//                        requestFields(
//                                fieldWithPath("groupName").description("자격 단체 이름"),
//                                fieldWithPath("certificateKind").description("자격증 종류"),
//                                fieldWithPath("region").description("강의 지역"),
//                                fieldWithPath("costCondition.max").description("강의료 최대 비용"),
//                                fieldWithPath("costCondition.min").description("강의료 최소 비용")
//                        ),
//                        responseHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
//                        ),
//                        responseFields(
//                                fieldWithPath("_embedded.lectureSearchResultList[0].id").description("강의 식별자 ID"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].title").description("강의 제목"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].classKind").description("강의 유형"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].groupName").description("강의 유형"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].certificateKind").description("자격증 종류"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].price").description("강의 비용"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].region").description("강의 지역"),
//                                fieldWithPath("_embedded.lectureSearchResultList[0].imageURL").description("강의 이미지들"),
//                                fieldWithPath("_links.first.href").description("첫 번째 페이지 URL"),
//                                fieldWithPath("_links.prev.href").description("이전 번째 페이지 URL"),
//                                fieldWithPath("_links.self.href").description("현재 페이지 URL"),
//                                fieldWithPath("_links.next.href").description("다음 페이지 URL"),
//                                fieldWithPath("_links.last.href").description("마지막 페이지 URL"),
//                                fieldWithPath("page.size").description("한 페이지당 크기"),
//                                fieldWithPath("page.totalElements").description("해당 지역 전체 강의 수"),
//                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
//                                fieldWithPath("page.number").description("현재 페이지 번호")
//                        )
//                ));
//    }
//
//    private Page<Lecture> createLecturePage(Pageable pageable) {
//        List<Lecture> lectureList = new ArrayList<>();
//        for (long i = 0; i < 15; i++) {
//            LectureImage lectureImage = LectureImage.builder()
//                    .fileURI("Image URL 주소")
//                    .build();
//
//            Lecture lecture = Lecture.builder()
//                    .id(i)
//                    .title("강의")
//                    .description("내용")
//                    .classKind("스쿠버 다이빙")
//                    .organization(Organization.AIDA)
//                    .level("Level1")
//                    .price(100000)
//                    .region("서울")
//                    .lectureImages(List.of(lectureImage))
//                    .build();
//            lectureList.add(lecture);
//        }
//
//        long endIndex = (pageable.getOffset() + pageable.getPageSize()) > lectureList.size() ?
//                lectureList.size() : pageable.getOffset() + pageable.getPageSize();
//
//        return new PageImpl<>(lectureList.subList((int) pageable.getOffset(), (int) endIndex), pageable, lectureList.size());
//    }
//
//    @Test
//    @DisplayName("강사 자신의 강의 목록 조회")
//    public void getLectureList() throws Exception {
//        Account account = createAccount();
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
//
//        Pageable pageable = PageRequest.of(0, 5);
//        Page<LectureInfo> lectureInfoPage = createLectureInfoPage(pageable);
//
//        given(lectureService.getMyLectureInfoList(account, pageable)).willReturn(lectureInfoPage);
//
//        mockMvc.perform(get("/lecture/manage/list")
//                .param("page", String.valueOf(pageable.getPageNumber()))
//                .param("size", String.valueOf(pageable.getPageSize()))
//                .contentType(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("lecture-get-list-per-instructor",
//                        requestHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
//                                headerWithName("Authorization").description("access token 값"),
//                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
//                        ),
//                        requestParameters(
//                                parameterWithName("page").description("몇 번째 페이지"),
//                                parameterWithName("size").description("한 페이지당 크기")
//                        ),
//                        responseFields(
//                                fieldWithPath("_embedded.lectureInfoList[].lectureId").description("강의 식별자 값"),
//                                fieldWithPath("_embedded.lectureInfoList[].title").description("강의 제목"),
//                                fieldWithPath("_embedded.lectureInfoList[].groupName").description("소속 그룹"),
//                                fieldWithPath("_embedded.lectureInfoList[].certificateKind").description("자격증 종류"),
//                                fieldWithPath("_embedded.lectureInfoList[].cost").description("강의 비용"),
//                                fieldWithPath("_embedded.lectureInfoList[].isRentEquipment").description("장비 대여 여부"),
//                                fieldWithPath("_embedded.lectureInfoList[].upcomingScheduleCount").description("다가오는 일정의 수"),
//                                fieldWithPath("_links.self.href").description("현재 페이지 URL"),
//                                fieldWithPath("page.size").description("한 페이지당 크기"),
//                                fieldWithPath("page.totalElements").description("해당 지역 전체 강의 수"),
//                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
//                                fieldWithPath("page.number").description("현재 페이지 번호")
//                        )
//                ));
//    }
//
//    private Page<LectureInfo> createLectureInfoPage(Pageable pageable) {
//        List<LectureInfo> lectureInfoList = new ArrayList<>();
//
//        LectureInfo lectureInfo = LectureInfo.builder()
//                .title("프리 다이빙 세상")
//                .organization(Organization.AIDA)
//                .level("Level1")
//                .cost(100000)
//                .upcomingScheduleCount(5)
//                .isRentEquipment(true)
//                .build();
//
//        lectureInfoList.add(lectureInfo);
//
//        return new PageImpl<>(lectureInfoList, pageable, lectureInfoList.size());
//    }

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
                .region("서울")
                .lectureTime(LocalTime.of(2, 30))
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
                                        fieldWithPath("maxNumber").description("강의 최대 인원수"),
                                        fieldWithPath("lectureTime").description("강의 총 소요 시간")
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
                    .maxNumber(10)
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
                                        fieldWithPath("_embedded.newLectureInfoList[].maxNumber").description("신규 강의 최대 인원수"),
                                        fieldWithPath("_embedded.newLectureInfoList[].maxNumber").description("신규 강의 최대 인원수"),
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

        List<PopularLectureInfo> lectureInfos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            PopularLectureInfo popularLectureInfo = PopularLectureInfo.builder()
                    .id((long) i)
                    .title("title" + i)
                    .organization(Organization.AIDA)
                    .level("Level1")
                    .region("Seoul")
                    .maxNumber(10)
                    .lectureTime(LocalTime.of(1, 30))
                    .imageUrl("Url" + i)
                    .isMarked(false)
                    .equipmentNames(List.of("아쿠아 슈즈", "슈트"))
                    .starAvg(4.5f)
                    .reviewCount(100)
                    .build();
            lectureInfos.add(popularLectureInfo);
        }
        Page<PopularLectureInfo> newLectureInfoPage = new PageImpl<>(lectureInfos, pageable, lectureInfos.size());

        given(lectureService.getPopularLecturesInfo(account, pageable)).willReturn(newLectureInfoPage);

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
                                        fieldWithPath("_embedded.popularLectureInfoList[].id").description("인기 강의 식별자 값"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].title").description("인기 강의 제목"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].organization").description("인기 강의 자격증 단체 이"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].level").description("인기 강의 자격증 레벨"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].region").description("인기 강의 지역명"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].maxNumber").description("인기 강의 최대 인원수"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].maxNumber").description("인기 강의 최대 인원수"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].lectureTime").description("인기 강의 총 강의 시간"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].isMarked").description("인기 강의 찜 여부"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].price").description("인기 강의 강의 비용"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].imageUrl").description("인기 강의 대표 이미지"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].equipmentNames[]").description("인기 강의 대여 장비 목록"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].starAvg").description("인기 강의 리뷰 종합 평점"),
                                        fieldWithPath("_embedded.popularLectureInfoList[].reviewCount").description("인기 강의 리뷰 갯수"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("page.size").description("한 페이지 당 사이즈"),
                                        fieldWithPath("page.totalElements").description("전체 신규 강의 갯수"),
                                        fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                        fieldWithPath("page.number").description("현재 페이지 번호")
                                )
                        )
                );
    }
}