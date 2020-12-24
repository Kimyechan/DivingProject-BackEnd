package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.ExceptionAdvice;
import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.diving.pungdong.controller.sign.SignController.AccountDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class SignControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockBean
    AccountService accountService;

    @Test
    @DisplayName("회원가입 성공")
    public void signupSuccess() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .email("yechan@gmail.com")
                .password("1234")
                .userName("yechan")
                .age(24)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        mockMvc.perform(post("/sign/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("signUp",
                        requestHeaders(
//                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("email").description("user id of account"),
                                fieldWithPath("password").description("user password of account"),
                                fieldWithPath("userName").description("user name of account"),
                                fieldWithPath("age").description("user age of account"),
                                fieldWithPath("gender").description("user gender of account"),
                                fieldWithPath("roles").description("user authorities of account")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header")
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 성공")
    public void signinSuccess() throws Exception {
        String email = "yechan@gmail.com";
        String password = "1234";
        String encodedPassword = passwordEncoder.encode(password);

        Account account = Account.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        given(accountService.findAccountByEmail(email)).willReturn(account);

        mockMvc.perform(post("/sign/signin")
                            .param("email", email)
                            .param("password", password))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("accessToken").exists())
                    .andExpect(jsonPath("_links.self").exists());
    }


}