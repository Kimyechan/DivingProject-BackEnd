package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.controller.lecture.LectureController.CreateLectureReq;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.service.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@Transactional
class LectureControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired ModelMapper modelMapper;
    @MockBean InstructorService instructorService;
    @MockBean SwimmingPoolService swimmingPoolService;
    @MockBean AccountService accountService;
    @MockBean LectureService lectureService;
    @MockBean LectureImageService lectureImageService;
    @MockBean S3Uploader s3Uploader;

    @Test
    @DisplayName("강의 개설")
    public void createLecture() throws Exception {
        Account account = createAccount();
        CreateLectureReq createLectureReq = CreateLectureReq.builder()
                .title("강의1")
                .description("내용1")
                .classKind("스쿠버 다이빙")
                .groupName("AIDA")
                .certificateKind("Level1")
                .price(100000)
                .period(4)
                .studentCount(5)
                .region("서울")
                .swimmingPoolId(1L)
                .build();

        MockMultipartFile file1 = new MockMultipartFile("fileList", "test1.txt", "image/*", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("fileList", "test2.txt", "image/*", "test data".getBytes());

        MockMultipartFile request =
                new MockMultipartFile("request",
                        "request",
                        MediaType.APPLICATION_JSON_VALUE,
                        objectMapper.writeValueAsString(createLectureReq).getBytes());
        Instructor instructor = modelMapper.map(account, Instructor.class);
        String accessToken = jwtTokenProvider.createAccessToken("1", Set.of(Role.INSTRUCTOR));
        SwimmingPool swimmingPool = new SwimmingPool();

        Lecture lecture = Lecture.builder()
                .title(createLectureReq.getTitle())
                .instructor(instructor)
                .build();

        given(instructorService.getInstructorByEmail(account.getEmail())).willReturn(instructor);
        given(swimmingPoolService.getSwimmingPool(1L)).willReturn(swimmingPool);
        given(lectureService.saveLectureAndImage(eq(account.getEmail()), anyList(), any(Lecture.class))).willReturn(lecture);

        mockMvc.perform(multipart("/lecture/create")
                            .file(file1)
                            .file(file2)
                            .file(request)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .header("IsRefreshToken", "false")
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andDo(document("create-lecture",
                            requestHeaders(
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("multipart form data 타입"),
                                    headerWithName("Authorization").description("access token 값"),
                                    headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                            ),
                            requestParts(
                                    partWithName("fileList").description("이미지 파일 리스트"),
                                    partWithName("request").description("강의 생성 정보 JSON 데이터")
                            ),
                            requestPartBody("fileList"),
                            requestPartBody("request"),
                            requestPartFields("request",
                                    fieldWithPath("title").description("강의 제목"),
                                    fieldWithPath("description").description("강의 내용"),
                                    fieldWithPath("classKind").description("강의 종류"),
                                    fieldWithPath("groupName").description("단체명"),
                                    fieldWithPath("certificateKind").description("자격증 종류"),
                                    fieldWithPath("price").description("강의 비용"),
                                    fieldWithPath("period").description("강의 기간"),
                                    fieldWithPath("studentCount").description("수강 인원 제한"),
                                    fieldWithPath("region").description("강의 지역"),
                                    fieldWithPath("swimmingPoolId").description("수영장 식별자 ID")
                            ),
                            responseHeaders(
                                    headerWithName(HttpHeaders.LOCATION).description("해당 API URI"),
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                            ),
                            responseFields(
                                    fieldWithPath("title").description("강의 제목"),
                                    fieldWithPath("instructorName").description("강사 이름"),
                                    fieldWithPath("_links.self.href").description("해당 API URI"),
                                    fieldWithPath("_links.profile.href").description("해당 API 문서 링크")
                            )
                    ));
    }

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
                .willReturn(new User(account.getEmail(), account.getPassword(), authorities(account.getRoles())));

        return account;
    }

    private Collection<? extends GrantedAuthority> authorities(Set<Role> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("이미지 파일 업로드")
    public void upload() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        given(s3Uploader.upload(file, "lecture", account.getEmail())).willReturn("image file aws s3 url");

        mockMvc.perform(multipart("/lecture/upload")
                                .file(file)
                                .header(HttpHeaders.AUTHORIZATION, accessToken)
                                .header("IsRefreshToken", "false"))
                                .andDo(print())
                                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지역별 강의 목록")
    public void getLectureListByRegion() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        String region = "서울";
        Pageable pageable = PageRequest.of(1, 5);
        given(lectureService.getListByRegion(region, pageable)).willReturn(createLectureList(pageable));

        mockMvc.perform(get("/lecture/list/region")
                    .param("region", region)
                    .param("page", String.valueOf(pageable.getPageNumber()))
                    .param("size", String.valueOf(pageable.getPageSize()))
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .header("IsRefreshToken", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(document("get-lecture-by-region",
                            requestHeaders(
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("accessToken 값"),
                                    headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                            ),
                            requestParameters(
                                    parameterWithName("region").description("강의 지역"),
                                    parameterWithName("page").description("몇 번째 페이지"),
                                    parameterWithName("size").description("한 페이지당 크기")
                            ),
                            responseHeaders(
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                            ),
                            responseFields(
                                    fieldWithPath("_embedded.lectureByRegionResList[0].id").description("강의 식별자 ID"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].title").description("강의 제목"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].classKind").description("강의 유형"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].groupName").description("강의 유형"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].certificateKind").description("자격증 종류"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].price").description("강의 비용"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].region").description("강의 지역"),
                                    fieldWithPath("_embedded.lectureByRegionResList[0].imageURL").description("강의 이미지들"),
                                    fieldWithPath("_links.first.href").description("첫 번째 페이지 URL"),
                                    fieldWithPath("_links.prev.href").description("이전 번째 페이지 URL"),
                                    fieldWithPath("_links.self.href").description("현재 페이지 URL"),
                                    fieldWithPath("_links.next.href").description("다음 페이지 URL"),
                                    fieldWithPath("_links.last.href").description("마지막 페이지 URL"),
                                    fieldWithPath("page.size").description("한 페이지당 크기"),
                                    fieldWithPath("page.totalElements").description("해당 지역 전체 강의 수"),
                                    fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("page.number").description("현재 페이지 번호")
                            )
                    ));
    }

    private Page<Lecture> createLectureList(Pageable pageable) {
        List<Lecture> lectureList = new ArrayList<>();
        for (long i = 0; i < 15; i++) {
            LectureImage lectureImage = LectureImage.builder()
                    .fileURI("Image URL 주소")
                    .build();

            Lecture lecture = Lecture.builder()
                    .id(i)
                    .title("강의")
                    .description("내용")
                    .classKind("스쿠버 다이빙")
                    .groupName("AIDA")
                    .certificateKind("Level1")
                    .price(100000)
                    .period(4)
                    .studentCount(5)
                    .region("서울")
                    .lectureImage(List.of(lectureImage))
                    .build();
            lectureList.add(lecture);
        }

        return new PageImpl<>(lectureList, pageable, lectureList.size());
    }
}