package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.ExceptionAdvice;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.diving.pungdong.controller.sign.SignController.AccountDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
                .andExpect(status().isCreated());
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