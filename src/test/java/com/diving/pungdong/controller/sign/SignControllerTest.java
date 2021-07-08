package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.config.EmbeddedRedisConfig;
import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.dto.account.emailCheck.EmailInfo;
import com.diving.pungdong.dto.account.emailCheck.EmailResult;
import com.diving.pungdong.dto.account.instructor.InstructorConfirmInfo;
import com.diving.pungdong.dto.account.instructor.InstructorConfirmResult;
import com.diving.pungdong.dto.account.instructor.InstructorInfo;
import com.diving.pungdong.dto.account.instructor.InstructorRequestInfo;
import com.diving.pungdong.service.kafka.dto.account.FirebaseTokenDto;
import com.diving.pungdong.dto.account.nickNameCheck.NickNameResult;
import com.diving.pungdong.dto.account.signIn.SignInInfo;
import com.diving.pungdong.dto.account.signUp.SignUpInfo;
import com.diving.pungdong.dto.account.signUp.SignUpResult;
import com.diving.pungdong.dto.auth.AuthToken;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.AuthService;
import com.diving.pungdong.service.InstructorCertificateService;
import com.diving.pungdong.service.kafka.AccountKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.diving.pungdong.controller.sign.SignController.LogoutReq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class, EmbeddedRedisConfig.class})
class SignControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    AccountService accountService;

    @MockBean
    AuthService authService;

    @MockBean
    InstructorCertificateService instructorCertificateService;

    @MockBean
    AccountKafkaProducer producer;

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

    private Collection<? extends GrantedAuthority> authorities(Set<Role> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    public void checkEmailExistence() throws Exception {
        EmailInfo emailInfo = EmailInfo.builder()
                .email("kim@gmail.com")
                .build();

        EmailResult emailResult = EmailResult.builder()
                .existed(true)
                .build();

        given(accountService.checkEmailExistence(any())).willReturn(emailResult);

        mockMvc.perform(post("/sign/check/email")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(emailInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("account-check-email",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("JSON 타입")
                                ),
                                requestFields(
                                        fieldWithPath("email").description("유저 이메일")
                                ),
                                responseFields(
                                        fieldWithPath("existed").description("유저 이메일 존재 여부"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("닉네임 중복 여부 확인")
    public void checkDuplicationNickName() throws Exception {
        String nickName = "닉네임";
        NickNameResult nickNameResult = NickNameResult.builder()
                .isExisted(false)
                .build();

        given(accountService.checkDuplicationOfNickName(nickName)).willReturn(nickNameResult);

        mockMvc.perform(get("/sign/check/nickName")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("nickName", nickName))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("account-check-duplication-nickName",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("JSON 타입")
                                ),
                                requestParameters(
                                        parameterWithName("nickName").description("닉네임")
                                ),
                                responseFields(
                                        fieldWithPath("isExisted").description("유저 닉네임 존재 여부"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("회원가입 성공 - 수강생 권한으로만 가입됨")
    public void signupInstructorSuccess() throws Exception {
        SignUpInfo signUpInfo = SignUpInfo.builder()
                .email("yechan@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .phoneNumber("010-1111-2222")
                .verifyCode("111222")
                .build();

        SignUpResult signUpResult = SignUpResult.builder()
                .email(signUpInfo.getEmail())
                .nickName(signUpInfo.getNickName())
                .build();

        given(accountService.saveAccountInfo(signUpInfo)).willReturn(signUpResult);

        mockMvc.perform(post("/sign/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(signUpInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("nickName").exists())
                .andDo(document("signUp",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("JSON 타입")
                        ),
                        requestFields(
                                fieldWithPath("email").description("유저 ID"),
                                fieldWithPath("password").description("유저 PASSWORD"),
                                fieldWithPath("nickName").description("유저의 닉네임"),
                                fieldWithPath("birth").description("유저의 생년월일"),
                                fieldWithPath("gender").description("유저의 성별"),
                                fieldWithPath("phoneNumber").description("휴대폰 번호"),
                                fieldWithPath("verifyCode").description("이메일 승인 코드")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("API 주소"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                        ),
                        responseFields(
                                fieldWithPath("email").description("유저 ID"),
                                fieldWithPath("nickName").description("유저의 닉네임"),
                                fieldWithPath("_links.self.href").description("해당 API 링크"),
                                fieldWithPath("_links.profile.href").description("API 문서 링크"),
                                fieldWithPath("_links.login.href").description("로그인 링크")
                        )
                ));
    }

    @Test
    @DisplayName("회원 가입 실패 - 입력값이 잘못됨")
    public void signupInputNull() throws Exception {
        SignUpInfo signUpInfo = SignUpInfo.builder().build();

        mockMvc.perform(post("/sign/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(signUpInfo)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(-1004))
                .andExpect(jsonPath("success").value(false));
    }

    @Test
    @DisplayName("로그인 성공")
    public void loginSuccess() throws Exception {
        String email = "yechan@gmail.com";
        String password = "1234";

        SignInInfo signInInfo = new SignInInfo(email, password);

        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password(passwordEncoder.encode(password))
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(Role.STUDENT))
                .build();

        AuthToken authToken = AuthToken.builder()
                .access_token("accessToken")
                .refresh_token("refreshToken")
                .token_type("tokenType")
                .scope("read")
                .expires_in(10000)
                .jti("jti")
                .build();

        given(authService.getAuthToken(String.valueOf(account.getId()), signInInfo.getPassword())).willReturn(authToken);
        given(accountService.findAccountByEmail(signInInfo.getEmail())).willReturn(account);

        mockMvc.perform(post("/sign/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("signIn",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("JSON 타입")
                        ),
                        requestFields(
                                fieldWithPath("email").description("유저 ID"),
                                fieldWithPath("password").description("유저 PASSWORD")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                        ),
                        responseFields(
                                fieldWithPath("access_token").description("JWT 인증 토큰 값"),
                                fieldWithPath("refresh_token").description("JWT Refresh Token 값"),
                                fieldWithPath("token_type").description("토큰 타입"),
                                fieldWithPath("scope").description("토큰 권한 범위"),
                                fieldWithPath("expires_in").description("유효 기간"),
                                fieldWithPath("jti").description("JWT 토큰 식별자"),
                                fieldWithPath("_links.self.href").description("해당 API 링크"),
                                fieldWithPath("_links.profile.href").description("API 문서 링크")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일(ID)이 없는 경우")
    public void loginNotFoundEmail() throws Exception {
        String email = "yechan@gmail.com";
        String password = "1234";

        doThrow(new CEmailSigninFailedException()).when(accountService).findAccountByEmail(email);

        mockMvc.perform(post("/sign/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignInInfo(email, password))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("code").value(-1001));
    }

    @Test
    @DisplayName("로그인 실패 - PASSWORD가 틀린 경우")
    public void loginNotMatchPassword() throws Exception {
        String email = "yechan@gmail.com";
        String password = "1234";
        String encodedPassword = passwordEncoder.encode(password);

        Account account = Account.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        given(accountService.findAccountByEmail(email)).willReturn(account);
        doThrow(new CEmailSigninFailedException()).when(accountService).checkCorrectPassword(any(), any());

        mockMvc.perform(post("/sign/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SignInInfo(email, "wrongPassword"))))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("code").value(-1001));
    }

    @Test
    @DisplayName("firebase token 등록")
    public void enrollFirebaseToken() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        FirebaseTokenDto firebaseTokenDto = FirebaseTokenDto.builder()
                .token("abcd12341234")
                .build();

        mockMvc.perform(post("/sign/firebase-token")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firebaseTokenDto)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "sign-enroll-firebase-token",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("token").description("firebase token 값")
                                )
                        )
                );
    }


    @Test
    @DisplayName("로그아웃 성공")
    public void logout() throws Exception {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new User(account.getEmail(), account.getPassword(), authorities(account.getRoles())));

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(account.getId()));

        LogoutReq logoutReq = LogoutReq.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/sign/logout")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").exists())
                .andDo(document("logout",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")),
                        requestFields(
                                fieldWithPath("accessToken").description("access token 값"),
                                fieldWithPath("refreshToken").description("refresh token 값")),
                        responseFields(
                                fieldWithPath("message").description("성공 메세지"),
                                fieldWithPath("_links.self.href").description("해당 API 주소"),
                                fieldWithPath("_links.profile.href").description("해당 API 문서 링크")
                        )
                ));
    }

    @Test
    @DisplayName("강사 기본 정보 추가")
    public void addInstructorInfo() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        InstructorInfo instructorInfo = InstructorInfo.builder()
                .organization(Organization.AIDA)
                .selfIntroduction("10년차 프리 다이빙 강사입니다.")
                .build();

        SuccessResult successResult = SuccessResult.builder()
                .success(true)
                .build();

        given(accountService.saveInstructorInfo(account, instructorInfo)).willReturn(successResult);

        mockMvc.perform(post("/sign/instructor/info")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(instructorInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-add-instructorInfo",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("JSON 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("organization").description("소속 단체"),
                                        fieldWithPath("selfIntroduction").description("강사 자기 소개")
                                ),
                                responseFields(
                                        fieldWithPath("success").description("승인 코드 전송 성공 여부"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강사 자격증 이미지 추가")
    public void addInstructorCertificateImage() throws Exception {
        Account account = createAccount(Role.INSTRUCTOR);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        MockMultipartFile file1 = new MockMultipartFile("certificateImages", "test1", "image/png", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("certificateImages", "test2", "image/png", "test data".getBytes());

        SuccessResult successResult = SuccessResult.builder()
                .success(true)
                .build();

        given(instructorCertificateService.saveInstructorCertificate(any(), any())).willReturn(successResult);

        mockMvc.perform(multipart("/sign/instructor/certificate")
                .file(file1)
                .file(file2)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-add-instructor-certificate",
                                requestHeaders(
                                        headerWithName(org.apache.http.HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(org.apache.http.HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParts(
                                        partWithName("certificateImages").description("강의 이미지들")
                                ),
                                responseFields(
                                        fieldWithPath("success").description("승인 코드 전송 성공 여부"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }

    @Test
    @DisplayName("관리자가 강사 신청 계정 목록 조회")
    public void getRequestOfInstructor() throws Exception {
        Account account = createAccount(Role.ADMIN);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        Pageable pageable = PageRequest.of(0, 2);

        List<InstructorRequestInfo> instructorRequestInfos = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            InstructorRequestInfo instructorRequestInfo = InstructorRequestInfo.builder()
                    .accountId((long) i)
                    .email("kim@gmail.com")
                    .phoneNumber("010-1111-2222")
                    .nickName("kim")
                    .organization(Organization.AIDA)
                    .selfIntroduction("자기소개")
                    .certificateImageUrls(List.of("자격증 이미지 Url"))
                    .build();
            instructorRequestInfos.add(instructorRequestInfo);
        }
        Page<InstructorRequestInfo> instructorRequestInfoPage = new PageImpl<>(instructorRequestInfos, pageable, instructorRequestInfos.size());

        given(accountService.getRequestInstructor(pageable)).willReturn(instructorRequestInfoPage);

        mockMvc.perform(get("/sign/instructor/request/list")
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-instructor-get-request-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("관리자 계정 access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.instructorRequestInfoList[].accountId").description("강사 신청자 식별자 값"),
                                        fieldWithPath("_embedded.instructorRequestInfoList[].email").description("강사 신청자 이메일"),
                                        fieldWithPath("_embedded.instructorRequestInfoList[].nickName").description("강사 신청자 닉네임"),
                                        fieldWithPath("_embedded.instructorRequestInfoList[].phoneNumber").description("강사 신청자 휴대폰 번호"),
                                        fieldWithPath("_embedded.instructorRequestInfoList[].organization").description("강사 신청자 소속 단체"),
                                        fieldWithPath("_embedded.instructorRequestInfoList[].selfIntroduction").description("강사 신청자 자기 소개"),
                                        fieldWithPath("_embedded.instructorRequestInfoList[].certificateImageUrls[]").description("강사 신청자 강사 자격증"),
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
    @DisplayName("관리자가 강사 권한 승인")
    public void confirmInstructor() throws Exception {
        Account account = createAccount(Role.ADMIN);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        InstructorConfirmInfo instructorConfirmInfo = InstructorConfirmInfo.builder()
                .accountId(account.getId())
                .build();

        InstructorConfirmResult instructorConfirmResult = InstructorConfirmResult.builder()
                .email(account.getEmail())
                .nickName(account.getNickName())
                .build();

        given(accountService.addInstructorRole(any())).willReturn(Account.builder().build());
        given(accountService.mapToInstructorConfirmResult(any())).willReturn(instructorConfirmResult);

        mockMvc.perform(
                put("/sign/instructor/confirm")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .content(objectMapper.writeValueAsString(instructorConfirmInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-instructor-confirm",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("관리자 access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("accountId").description("강사 권한을 획들할 계정 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("email").description("강사 권한을 얻은 계정 이메일"),
                                        fieldWithPath("nickName").description("강사 권한을 얻은 계정 닉네임"),
                                        fieldWithPath("_links.self.href").description("해당 API 링크"),
                                        fieldWithPath("_links.profile.href").description("API 문서 링크")
                                )
                        )
                );
    }
}