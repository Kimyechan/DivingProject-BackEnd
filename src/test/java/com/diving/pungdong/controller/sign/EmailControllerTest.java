package com.diving.pungdong.controller.sign;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.dto.account.emailCode.EmailAuthInfo;
import com.diving.pungdong.dto.account.emailCode.EmailSendInfo;
import com.diving.pungdong.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
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
class EmailControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("이메일 인증코드 전송")
    public void sendEmailCode() throws Exception {
        EmailSendInfo emailSendInfo = EmailSendInfo.builder()
                .email("kim@gmail.com")
                .build();

        mockMvc.perform(post("/email/code/send")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(emailSendInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "account-email-auth-code-send",
                                requestHeaders(
                                        headerWithName(org.springframework.http.HttpHeaders.CONTENT_TYPE).description("JSON 타입")
                                ),
                                requestFields(
                                        fieldWithPath("email").description("유저 이메일")
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
    @DisplayName("이메일 승인 코드 검증")
    public void verifyEmailCode() throws Exception {
        EmailAuthInfo emailAuthInfo = EmailAuthInfo.builder()
                .email("kim@gmail.com")
                .code("123456")
                .build();

        mockMvc.perform(post("/email/code/verify")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(emailAuthInfo)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}