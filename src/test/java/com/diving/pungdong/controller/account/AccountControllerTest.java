package com.diving.pungdong.controller.account;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.dto.account.instructor.certificate.InstructorCertificateInfo;
import com.diving.pungdong.dto.account.read.InstructorBasicInfo;
import com.diving.pungdong.service.InstructorCertificateService;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.dto.account.read.AccountBasicInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AccountService accountService;

    @MockBean
    private InstructorCertificateService instructorCertificateService;


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
    @DisplayName("계정 정보 조회")
    public void readAccountInfo() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        AccountBasicInfo accountBasicInfo = AccountBasicInfo.builder()
                .id(1L)
                .email("kim@gmail")
                .nickName("kim")
                .phoneNumber("010-1111-2222")
                .birth("1997-08-15")
                .gender(Gender.MALE)
                .build();

        given(accountService.mapToAccountBasicInfo(any())).willReturn(accountBasicInfo);

        mockMvc.perform(get("/account")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-read",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("계정 식별자 값"),
                                        fieldWithPath("email").description("이메일"),
                                        fieldWithPath("nickName").description("닉네임"),
                                        fieldWithPath("phoneNumber").description("폰 번호"),
                                        fieldWithPath("birth").description("생년월일"),
                                        fieldWithPath("gender").description("성별"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }


    @Test
    @DisplayName("강사 정보 조회")
    public void readInstructorInfo() throws Exception {
        Account account = createAccount(Role.INSTRUCTOR);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        InstructorBasicInfo instructorBasicInfo = InstructorBasicInfo.builder()
                .id(1L)
                .organization(Organization.AIDA)
                .selfIntroduction("강사 자기 소개")
                .build();

        given(accountService.mapToInstructorBasicInfo(any())).willReturn(instructorBasicInfo);

        mockMvc.perform(get("/account/instructor")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-instructor-read",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("계정 식별자 값"),
                                        fieldWithPath("organization").description("소속 단체"),
                                        fieldWithPath("selfIntroduction").description("강사 자기 소개"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강사 자격증 목록 조회")
    public void readInstructorCertificates() throws Exception {
        Account account = createAccount(Role.INSTRUCTOR);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        List<InstructorCertificateInfo> certificateInfoList = new ArrayList<>();
        InstructorCertificateInfo certificateInfo = InstructorCertificateInfo.builder()
                .id(1L)
                .imageUrl("자격증 이미지 URL")
                .build();
        certificateInfoList.add(certificateInfo);

        given(instructorCertificateService.findInstructorCertificates(any())).willReturn(Collections.emptyList());
        given(instructorCertificateService.mapToInstructorCertificateInfos(any())).willReturn(certificateInfoList);

        mockMvc.perform(get("/account/instructor/certificate/list")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-instructor-certificate-read-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.instructorCertificateInfoList[].id").description("강사 자격증 사진 식별자 값"),
                                        fieldWithPath("_embedded.instructorCertificateInfoList[].imageUrl").description("자격증 사진 Url"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }
}