package com.diving.pungdong.controller.profilePhoto;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.profilePhoto.ProfilePhotoInfo;
import com.diving.pungdong.dto.profilePhoto.ProfilePhotoUpdateInfo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.account.ProfilePhotoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({RestDocsConfiguration.class})
class ProfilePhotoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ProfilePhotoService profilePhotoService;

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
    @DisplayName("프로필 이미지 수정")
    public void modifyProfileImage() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        MockMultipartFile file = new MockMultipartFile("image", "test1", "image/png", "test data".getBytes());

        ProfilePhotoUpdateInfo updateInfo = ProfilePhotoUpdateInfo.builder()
                .profilePhotoId(1L)
                .url("profile photo image url")
                .build();

        given(profilePhotoService.updateProfilePhoto(any(), any())).willReturn(updateInfo);

        mockMvc.perform(multipart("/profile-photo")
                .file(file)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "profilePhoto-update",
                                requestHeaders(
                                        headerWithName(org.apache.http.HttpHeaders.CONTENT_TYPE).description("multipart form data 타입"),
                                        headerWithName(org.apache.http.HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestParts(
                                        partWithName("image").description("회원 프로필 이미지")
                                ),
                                responseFields(
                                        fieldWithPath("profilePhotoId").description("프로필 사진 식별자 값"),
                                        fieldWithPath("url").description("프로필 사진 url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

    @Test
    @DisplayName("프로필 이미지 조회")
    public void readProfilePhoto() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        ProfilePhotoInfo profilePhotoInfo = ProfilePhotoInfo.builder()
                .profilePhotoId(1L)
                .imageUrl("프로필 이미지 URL")
                .build();

        given(profilePhotoService.findByAccount(any())).willReturn(profilePhotoInfo);

        mockMvc.perform(get("/profile-photo")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "profilePhoto-read",
                                requestHeaders(
                                        headerWithName(org.apache.http.HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                responseFields(
                                        fieldWithPath("profilePhotoId").description("프로필 사진 식별자 값"),
                                        fieldWithPath("imageUrl").description("프로필 사진 url"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }
}