package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateReq;
import com.diving.pungdong.dto.schedule.create.ScheduleDetailReq;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.ReservationService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @MockBean
    private LectureService lectureService;

    @Test
    @DisplayName("일정 등록")
    public void create() throws Exception {
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.INSTRUCTOR));

        ScheduleCreateReq req = ScheduleCreateReq.builder()
                .lectureId(1L)
                .period(3)
                .maxNumber(5)
                .build();

        List<ScheduleDetailReq> detailReqs = new ArrayList<>();
        for (int i = 1; i <= req.getPeriod(); i++) {
            Location location = Location.builder()
                    .address("상세 주소")
                    .latitude(37.0)
                    .longitude(127.0)
                    .build();
            List<LocalTime> startTimes = new ArrayList<>();
            startTimes.add(LocalTime.of(13, 0));
            startTimes.add(LocalTime.of(15, 0));

            ScheduleDetailReq scheduleDetailReq = ScheduleDetailReq.builder()
                    .date(LocalDate.of(2021, 2, i))
                    .startTimes(startTimes)
                    .lectureTime(LocalTime.of(1, 30))
                    .location(location)
                    .build();

            detailReqs.add(scheduleDetailReq);
        }
        req.setDetailReqList(detailReqs);

        given(lectureService.getLectureById(req.getLectureId())).willReturn(Lecture.builder().id(1L).build());
        given(scheduleService.saveScheduleTx(any(), eq(req))).willReturn(Schedule.builder().id(1L).build());
        mockMvc.perform(post("/schedule")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("schedule-create",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName("Authorization").description("access token 값"),
                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                        ),
                        requestFields(
                                fieldWithPath("lectureId").description("강의 식별자 값"),
                                fieldWithPath("period").description("강의 기간"),
                                fieldWithPath("maxNumber").description("수강 제한 인원 수"),
                                fieldWithPath("detailReqList").description("강의 한 날에 대한 세부사항 리스트"),
                                fieldWithPath("detailReqList[].date").description("강의 날짜"),
                                fieldWithPath("detailReqList[].startTimes[]").description("강의 시작 시간 리스트"),
                                fieldWithPath("detailReqList[].lectureTime").description("강의 시간"),
                                fieldWithPath("detailReqList[].location.latitude").description("위치 위도"),
                                fieldWithPath("detailReqList[].location.longitude").description("위치 경도"),
                                fieldWithPath("detailReqList[].location.address").description("위치 상세 주소")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                        ),
                        responseFields(
                                fieldWithPath("lectureId").description("강의 식별자 값"),
                                fieldWithPath("scheduleId").description("일정 식별자 값"),
                                fieldWithPath("_links.self.href").description("해당 API 주소")
                        )
                ));
    }

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

    private Collection<? extends GrantedAuthority> authorities(Set<Role> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("해당 강의 일정 조회")
    public void getScheduleByLectureId() throws Exception {
        Long lectureId = 1L;

        List<Schedule> schedules = createSchedules();

        given(scheduleService.filterListByCheckingPast(lectureId)).willReturn(schedules);

        mockMvc.perform(get("/schedule")
                .param("lectureId", String.valueOf(lectureId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("schedule-read",
                        requestParameters(
                                parameterWithName("lectureId").description("강의 식별자 값")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleId").description("해당 일정 식별자 값"),
                                fieldWithPath("_embedded.scheduleDtoList[].period").description("총 강의 회차"),
                                fieldWithPath("_embedded.scheduleDtoList[].maxNumber").description("강의 최대 인원"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].scheduleDetailId").description("상세 일정 식별자 값"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].date").description("강의 날짜"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].scheduleTimeDtoList[].scheduleTimeId").description("강의 시간 식별자 값"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].scheduleTimeDtoList[].startTime").description("강의 시작 시간"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].scheduleTimeDtoList[].currentNumber").description("현재 신청한 인원 수"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].lectureTime").description("강의 진행 시간"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].location.latitude").description("강의 장소 위도"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].location.longitude").description("강의 장소 경도"),
                                fieldWithPath("_embedded.scheduleDtoList[].scheduleDetails[].location.address").description("강의 장소 주소"),
                                fieldWithPath("_links.self.href").description("해당 API URL")
                        )
                ));
    }

    public List<Schedule> createSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        Schedule schedule = Schedule.builder()
                .id(1L)
                .period(3)
                .maxNumber(10)
                .build();

        Location location = Location.builder()
                .latitude(37.0)
                .longitude(127.0)
                .address("상세 주소")
                .build();

        List<ScheduleDetail> scheduleDetails = createScheduleDetails(location, schedule.getPeriod());

        schedule.setScheduleDetails(scheduleDetails);
        schedules.add(schedule);
        return schedules;
    }

    public List<ScheduleDetail> createScheduleDetails(Location location, Integer period) {
        List<ScheduleDetail> scheduleDetails = new ArrayList<>();
        List<ScheduleTime> scheduleTimes = createScheduleTimes();
        for (int i = 0; i < period; i++) {
            ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                    .id(2L)
                    .date(LocalDate.of(2021, 3, 1).plusDays(i))
                    .scheduleTimes(scheduleTimes)
                    .lectureTime(LocalTime.of(1, 30))
                    .location(location)
                    .build();
            scheduleDetails.add(scheduleDetail);
        }
        return scheduleDetails;
    }

    public List<ScheduleTime> createScheduleTimes() {
        List<ScheduleTime> scheduleTimes = new ArrayList<>();
        ScheduleTime scheduleTime = ScheduleTime.builder()
                .id(3L)
                .startTime(LocalTime.of(13, 0))
                .currentNumber(5)
                .build();

        scheduleTimes.add(scheduleTime);
        return scheduleTimes;
    }
}