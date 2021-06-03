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
import com.diving.pungdong.dto.account.instructor.InstructorInfo;
import com.diving.pungdong.dto.account.nickNameCheck.NickNameResult;
import com.diving.pungdong.dto.account.signIn.SignInInfo;
import com.diving.pungdong.dto.account.signUp.SignUpInfo;
import com.diving.pungdong.dto.account.signUp.SignUpResult;
import com.diving.pungdong.dto.auth.AuthToken;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.service.AccountService;
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

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.diving.pungdong.controller.sign.SignController.LogoutReq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
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
                .header("IsRefreshToken", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutReq)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").exists())
                .andDo(document("logout",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값"),
                                headerWithName("IsRefreshToken").description("refresh token 인지 아닌지에 대한 값")),
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
                .andExpect(status().isOk());
    }

//    @Test
//    @DisplayName("금지된 토큰으로 접근시 실패 테스트")
//    public void forbidden() throws Exception {
//        Account account = Account.builder()
//                .id(1L)
//                .email("yechan@gmail.com")
//                .password("1234")
//                .roles(Set.of(Role.INSTRUCTOR))
//                .build();
//
//        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
//                .willReturn(new User(account.getEmail(), account.getPassword(), authorities(account.getRoles())));
//
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
//        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(account.getId()));
//
//        given(accountService.checkValidToken(accessToken)).willReturn("false");
//
//        LogoutReq logoutReq = LogoutReq.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//
//        mockMvc.perform(get("/sign/logout")
//                .header("Authorization", accessToken)
//                .header("IsRefreshToken", "false")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(logoutReq)))
//                .andDo(print())
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("code").value(-1007));
//    }
}