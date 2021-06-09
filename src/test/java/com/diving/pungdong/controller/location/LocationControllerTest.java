package com.diving.pungdong.controller.location;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.dto.location.LocationCreateInfo;
import com.diving.pungdong.dto.location.LocationCreateResult;
import com.diving.pungdong.service.LocationService;
import com.diving.pungdong.service.account.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class})
class LocationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private LocationService locationService;

    public Account createAccount() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(Role.STUDENT, Role.INSTRUCTOR))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new UserAccount(account));

        return account;
    }

    @Test
    @DisplayName("강의 위치 생성")
    public void createLectureLocation() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        LocationCreateInfo locationCreateInfo = LocationCreateInfo.builder()
                .address("서울특별시 송파구 오륜동 올림픽로 424 올림픽수영장")
                .latitude(37.519936106861635)
                .longitude(127.12643458977112)
                .lectureId(1L)
                .build();

        LocationCreateResult result = LocationCreateResult.builder()
                .lectureId(1L)
                .locationId(1L)
                .build();

        given(locationService.saveLocationWithLecture(account, locationCreateInfo)).willReturn(result);

        mockMvc.perform(post("/location/create")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(locationCreateInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("location-create",
                                requestHeaders(
                                        headerWithName(org.springframework.http.HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(org.springframework.http.HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("address").description("강의 위치 주소"),
                                        fieldWithPath("latitude").description("강의 위치 위도"),
                                        fieldWithPath("longitude").description("강의 위치 경도"),
                                        fieldWithPath("lectureId").description("위치를 지정할 강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("locationId").description("강의 위치 식별자 값"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 상세 조회시 강의 위치 정보 조회")
    public void findLectureLocation() throws Exception {
        Long lectureId = 1L;

        Location location = Location.builder()
                .id(1L)
                .address("서울특별시 송파구 오륜동 올림픽로 424 올림픽수영장")
                .latitude(37.519936106861635)
                .longitude(127.12643458977112)
                .build();

        given(locationService.findLocationByLectureId(lectureId)).willReturn(location);

        mockMvc.perform(get("/location")
                .param("lectureId", String.valueOf(lectureId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("location-find",
                                requestParameters(
                                        parameterWithName("lectureId").description("강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("강의 위치 식별자 값"),
                                        fieldWithPath("address").description("강의 위치 상세 주소"),
                                        fieldWithPath("latitude").description("강의 위치 위도"),
                                        fieldWithPath("longitude").description("강의 위치 경도"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                                )
                        )
                );
    }
}