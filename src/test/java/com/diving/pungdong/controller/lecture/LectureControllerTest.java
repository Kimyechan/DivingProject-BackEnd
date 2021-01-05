package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.controller.lecture.LectureController.CreateLectureReq;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.account.Role;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .kind("Level1")
                .price(100000)
                .period(4)
                .studentCount(5)
                .swimmingPoolId(1L)
                .build();

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "image/*", "test data".getBytes());
        MockMultipartFile request =
                new MockMultipartFile("request",
                        "request",
                        MediaType.APPLICATION_JSON_VALUE,
                        objectMapper.writeValueAsString(createLectureReq).getBytes());
        Instructor instructor = modelMapper.map(account, Instructor.class);
        String accessToken = jwtTokenProvider.createAccessToken("1", Set.of(Role.INSTRUCTOR));
        SwimmingPool swimmingPool = new SwimmingPool();

        given(instructorService.getInstructorByEmail(account.getEmail())).willReturn(instructor);
        given(swimmingPoolService.getSwimmingPool(1L)).willReturn(swimmingPool);
        given(s3Uploader.upload(file, "lecture")).willReturn("image file aws s3 url");

        mockMvc.perform(multipart("/lecture/create")
                            .file(file)
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
                                    partWithName("file").description("이미지 파일 데이터"),
                                    partWithName("request").description("강의 생성 정보 JSON 데이터")
                            ),
                            requestPartBody("file"),
                            requestPartBody("request"),
                            requestPartFields("request",
                                    fieldWithPath("title").description("강의 제목"),
                                    fieldWithPath("description").description("강의 내용"),
                                    fieldWithPath("kind").description("강의 종류"),
                                    fieldWithPath("price").description("강의 비용"),
                                    fieldWithPath("period").description("강의 기간"),
                                    fieldWithPath("studentCount").description("수강 인원 제한"),
                                    fieldWithPath("swimmingPoolId").description("수영장 식별자 ID")
                            ),
                            responseHeaders(
                                    headerWithName(HttpHeaders.LOCATION).description("해당 API URI"),
                                    headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                            ),
                            responseFields(
                                    fieldWithPath("title").description("강의 제목"),
                                    fieldWithPath("instructorName").description("강사 이름"),
                                    fieldWithPath("fileURI").description("이미지 파일 URI"),
                                    fieldWithPath("_links.self.href").description("해당 API URI"),
                                    fieldWithPath("_links.profile.href").description("해당 API 문서 링크")
                            )
                    ));
        verify(lectureService).saveLecture(any());
        verify(lectureImageService).saveLectureImage(any());
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
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        given(s3Uploader.upload(file, "lecture")).willReturn("image file aws s3 url");
        mockMvc.perform(multipart("/lecture/upload")
                                .file(file))
                                .andDo(print())
                                .andExpect(status().isOk());
    }
}