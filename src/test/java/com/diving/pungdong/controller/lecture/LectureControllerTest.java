package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.config.RestDocsConfiguration;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
    @MockBean private LectureImageService lectureImageService;

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
                .fileName("abc.png")
                .swimmingPoolId(1L)
                .build();

        Instructor instructor = modelMapper.map(account, Instructor.class);
        String accessToken = jwtTokenProvider.createAccessToken("1", Set.of(Role.INSTRUCTOR));
        SwimmingPool swimmingPool = new SwimmingPool();

        given(instructorService.getInstructorByEmail(account.getEmail())).willReturn(instructor);
        given(swimmingPoolService.getSwimmingPool(1L)).willReturn(swimmingPool);

        mockMvc.perform(post("/lecture/create")
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .header("IsRefreshToken", "false")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createLectureReq)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andDo(document("create-lecture",
                            requestHeaders(
                                    headerWithName("Authorization").description("access token 값"),
                                    headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                            ),
                            requestFields(
                                    fieldWithPath("title").description("강의 제목"),
                                    fieldWithPath("description").description("강의 내용"),
                                    fieldWithPath("kind").description("강의 종류"),
                                    fieldWithPath("price").description("강의 비용"),
                                    fieldWithPath("period").description("강의 기간"),
                                    fieldWithPath("studentCount").description("수강 인원 제한"),
                                    fieldWithPath("fileName").description("강의 사진 파일 이름"),
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
}