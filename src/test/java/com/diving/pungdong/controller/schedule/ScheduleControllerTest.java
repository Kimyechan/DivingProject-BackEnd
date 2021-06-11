package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleInfo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.ScheduleService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class ScheduleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ScheduleService scheduleService;

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
    @DisplayName("일정 등록")
    public void createSchedule() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.INSTRUCTOR));

        ScheduleCreateInfo scheduleCreateInfo = ScheduleCreateInfo.builder()
                .lectureId(1L)
                .period(3)
                .maxNumber(5)
                .startTime(LocalTime.of(11, 30))
                .dates(List.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), LocalDate.now().plusDays(8)))
                .build();

        given(scheduleService.saveScheduleInfo(any(), any())).willReturn(Schedule.builder().id(1L).build());

        mockMvc.perform(post("/schedule")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .content(objectMapper.writeValueAsString(scheduleCreateInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("schedule-create",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName("Authorization").description("강의 생성자 access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("period").description("강의 기간"),
                                        fieldWithPath("maxNumber").description("수강 제한 인원 수"),
                                        fieldWithPath("startTime").description("강의 시작 시간"),
                                        fieldWithPath("dates[]").description("강의 날짜 목록")
                                ),
                                responseFields(
                                        fieldWithPath("scheduleId").description("일정 식별자 값"),
                                        fieldWithPath("_links.self.href").description("해당 API 주소")
                                )
                        ));
    }


    @Test
    @DisplayName("해당 강의 일정 조회")
    public void findSchedulesByLectureId() throws Exception {
        Long lectureId = 1L;
        Month month = Month.JANUARY;

        List<ScheduleInfo> scheduleInfos = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            ScheduleInfo scheduleInfo = ScheduleInfo.builder()
                    .scheduleId((long) i)
                    .period(2)
                    .startTime(LocalTime.of(i, 30))
                    .currentNumber(5)
                    .maxNumber(10)
                    .dates(List.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)))
                    .build();
            scheduleInfos.add(scheduleInfo);
        }

        given(scheduleService.findLectureScheduleByMonth(lectureId, month, LocalDate.now())).willReturn(new ArrayList<>());
        given(scheduleService.mapToScheduleInfos(any())).willReturn(scheduleInfos);

        mockMvc.perform(get("/schedule")
                .param("lectureId", String.valueOf(lectureId))
                .param("month", String.valueOf(month.getValue())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("schedule-read-list",
                                requestParameters(
                                        parameterWithName("lectureId").description("강의 식별자 값"),
                                        parameterWithName("month").description("조회할 일정의 월 지정")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.scheduleInfoList[].scheduleId").description("강의 일정 식별자 값"),
                                        fieldWithPath("_embedded.scheduleInfoList[].period").description("강의 기간"),
                                        fieldWithPath("_embedded.scheduleInfoList[].startTime").description("강의 시작 시간"),
                                        fieldWithPath("_embedded.scheduleInfoList[].currentNumber").description("현재 일정에 등록된 수강생 수"),
                                        fieldWithPath("_embedded.scheduleInfoList[].maxNumber").description("해당 일정에 가능한 최대 수강생 수"),
                                        fieldWithPath("_embedded.scheduleInfoList[].dates[]").description("강의 일정 날짜 목록"),
                                        fieldWithPath("_links.self.href").description("해당 API 주소"),
                                        fieldWithPath("_links.profile.href").description("해당 API 문서 주소")
                                )
                        )
                );
    }
}