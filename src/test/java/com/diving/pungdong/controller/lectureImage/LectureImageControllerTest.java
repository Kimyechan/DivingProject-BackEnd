package com.diving.pungdong.controller.lectureImage;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.lectureImage.LectureImageInfo;
import com.diving.pungdong.dto.lectureImage.LectureImageUrl;
import com.diving.pungdong.dto.lectureImage.delete.LectureImageDeleteInfo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.LectureImageService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({RestDocsConfiguration.class})
class LectureImageControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AccountService accountService;

    @MockBean
    private LectureImageService lectureImageService;

    @MockBean
    private LectureEsService lectureEsService;

    public Account createAccount() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new UserAccount(account));

        return account;
    }

    @Test
    @DisplayName("강의 개설시 이미지들 등록")
    public void createImagesAboutLecture() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        MockMultipartFile file1 = new MockMultipartFile("images", "test1", "image/png", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("images", "test2", "image/png", "test data".getBytes());

        LectureImageInfo lectureImageInfo = LectureImageInfo.builder()
                .lectureId(1L)
                .imageUris(List.of("uri1", "uri2"))
                .build();

        given(lectureImageService.saveImages(any(), any(), any())).willReturn(lectureImageInfo);

        mockMvc.perform(multipart("/lectureImage/create/list")
                .file(file1)
                .file(file2)
                .param("lectureId", "1")
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document(
                                "lectureImage-create-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("multipart form data 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("lectureId").description("이미지들을 저장할 강의 식별자 값")
                                ),
                                requestParts(
                                        partWithName("images").description("강의 이미지들")
                                ),
                                responseFields(
                                        fieldWithPath("lectureId").description("개설된 강의 식별자 값"),
                                        fieldWithPath("imageUris[]").description("강의 이미지 Uri들"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 이미지 목록 조회")
    public void findLectureImages() throws Exception {
        Long lectureId = 1L;

        List<LectureImageUrl> urls = new ArrayList<>();
        LectureImageUrl lectureImageUrl = LectureImageUrl.builder()
                .url("강의 이미지 Url 1")
                .build();
        urls.add(lectureImageUrl);

        given(lectureImageService.findLectureImagesUrl(lectureId)).willReturn(urls);

        mockMvc.perform(get("/lectureImage/list")
                .param("lectureId", String.valueOf(lectureId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "lectureImage-find-list",
                                requestParameters(
                                        parameterWithName("lectureId").description("강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.lectureImageUrlList[].lectureImageId").description("강의 이미지 식별자 값"),
                                        fieldWithPath("_embedded.lectureImageUrlList[].url").description("강의 이미지 URL"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 이미지들 삭제")
    public void deleteLectureImages() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LectureImageDeleteInfo lectureImageDeleteInfo = LectureImageDeleteInfo.builder()
                .lectureId(1L)
                .lectureImageIds(List.of(1L, 2L))
                .build();

        mockMvc.perform(delete("/lectureImage/list")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(lectureImageDeleteInfo)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "lectureImage-delete-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("lectureImageIds[]").description("삭제될 image 식별자 목록")
                                )
                        )
                );
    }
}