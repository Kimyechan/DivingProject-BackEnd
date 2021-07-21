package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.create.ScheduleDateTimeCreateInfo;
import com.diving.pungdong.dto.schedule.equipment.RentEquipmentInfo;
import com.diving.pungdong.dto.schedule.equipment.RentEquipmentStockInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleDateTimeInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleInfo;
import com.diving.pungdong.dto.schedule.reservation.ReservationEquipmentInfo;
import com.diving.pungdong.dto.schedule.reservation.ReservationInfo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.schedule.ScheduleService;
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
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        ScheduleDateTimeCreateInfo scheduleDateTimeCreateInfo = ScheduleDateTimeCreateInfo.builder()
                .startTime(LocalTime.of(11, 30))
                .endTime(LocalTime.of(12, 30))
                .date(LocalDate.of(2021, 6, 30))
                .build();

        ScheduleCreateInfo scheduleCreateInfo = ScheduleCreateInfo.builder()
                .lectureId(1L)
                .dateTimeCreateInfos(List.of(scheduleDateTimeCreateInfo))
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
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("강의 생성자 access token 값")
                                ),
                                requestFields(
                                        fieldWithPath("lectureId").description("강의 식별자 값"),
                                        fieldWithPath("dateTimeCreateInfos[].startTime").description("강의 한 날짜의 시작 시간"),
                                        fieldWithPath("dateTimeCreateInfos[].endTime").description("강의 한 날짜의 종료 시간"),
                                        fieldWithPath("dateTimeCreateInfos[].date").description("강의 날짜")
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
        int year = 2021;
        Month month = Month.JANUARY;

        List<ScheduleInfo> scheduleInfos = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            ScheduleDateTimeInfo scheduleDateTimeInfo = ScheduleDateTimeInfo.builder()
                    .scheduleDateTimeId(1L)
                    .startTime(LocalTime.of(11, 30))
                    .endTime(LocalTime.of(12, 30))
                    .date(LocalDate.of(2021, 6, 30))
                    .build();

            ScheduleInfo scheduleInfo = ScheduleInfo.builder()
                    .scheduleId((long) i)
                    .currentNumber(5)
                    .maxNumber(10)
                    .dateTimeInfos(List.of(scheduleDateTimeInfo))
                    .build();
            scheduleInfos.add(scheduleInfo);
        }

        given(scheduleService.findLectureScheduleByMonth(lectureId, year, month, LocalDate.now())).willReturn(new ArrayList<>());
        given(scheduleService.mapToScheduleInfos(any())).willReturn(scheduleInfos);

        mockMvc.perform(get("/schedule")
                .param("lectureId", String.valueOf(lectureId))
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month.getValue())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("schedule-read-list",
                                requestParameters(
                                        parameterWithName("lectureId").description("강의 식별자 값"),
                                        parameterWithName("year").description("조회할 일정의 연도"),
                                        parameterWithName("month").description("조회할 일정의 월 지정")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.scheduleInfoList[].scheduleId").description("강의 일정 식별자 값"),
                                        fieldWithPath("_embedded.scheduleInfoList[].currentNumber").description("현재 일정에 등록된 수강생 수"),
                                        fieldWithPath("_embedded.scheduleInfoList[].maxNumber").description("수강 가능한 최대 인원 수"),
                                        fieldWithPath("_embedded.scheduleInfoList[].dateTimeInfos[].scheduleDateTimeId").description("강의 일정 날짜 식별자 값"),
                                        fieldWithPath("_embedded.scheduleInfoList[].dateTimeInfos[].startTime").description("강의 일정 한 날짜의 시작 시간"),
                                        fieldWithPath("_embedded.scheduleInfoList[].dateTimeInfos[].endTime").description("강의 일정 한 날짜의 종료 시간"),
                                        fieldWithPath("_embedded.scheduleInfoList[].dateTimeInfos[].date").description("강의 일정 날짜"),
                                        fieldWithPath("_links.self.href").description("해당 API 주소"),
                                        fieldWithPath("_links.profile.href").description("해당 API 문서 주소")
                                )
                        )
                );
    }

    @Test
    @DisplayName("해당 일정의 대여 장비 정보 조회")
    public void readScheduleEquipments() throws Exception {
        Long scheduleId = 1L;

        RentEquipmentStockInfo rentEquipmentStockInfo = RentEquipmentStockInfo.builder()
                .scheduleEquipmentStockId(1L)
                .size("L")
                .quantity(10)
                .totalRentNumber(4)
                .build();

        RentEquipmentInfo rentEquipmentInfo = RentEquipmentInfo.builder()
                .scheduleEquipmentId(1L)
                .name("오리발")
                .price(10000)
                .stockInfoList(List.of(rentEquipmentStockInfo))
                .build();

        List<RentEquipmentInfo> rentEquipmentInfos = new ArrayList<>();
        rentEquipmentInfos.add(rentEquipmentInfo);

        given(scheduleService.findScheduleEquipments(scheduleId)).willReturn(rentEquipmentInfos);

        mockMvc.perform(get("/schedule/equipments")
                .param("scheduleId", String.valueOf(scheduleId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("schedule-read-equipment-list",
                                requestParameters(
                                        parameterWithName("scheduleId").description("강의 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].scheduleEquipmentId").description("대여 장비 식별자 값"),
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].name").description("대여 장비 이름"),
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].price").description("대여 장비 가격"),
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].stockInfoList[].scheduleEquipmentStockId").description("대여 장비 재고 식별자 값"),
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].stockInfoList[].size").description("대여 장비 사이즈"),
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].stockInfoList[].quantity").description("대여 장비 갯수"),
                                        fieldWithPath("_embedded.rentEquipmentInfoList[].stockInfoList[].totalRentNumber").description("현재 대여 장비 대여 수"),
                                        fieldWithPath("_links.self.href").description("해당 API 주소"),
                                        fieldWithPath("_links.profile.href").description("해당 API 문서 주소")
                                )
                        )
                );
    }

    @Test
    @DisplayName("한 일정에 예약 정보 목록 조회")
    public void readReservationInfoForSchedule() throws Exception {
        Long scheduleId = 1L;

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<ReservationEquipmentInfo> reservationEquipmentInfos = new ArrayList<>();
        ReservationEquipmentInfo reservationEquipmentInfo = ReservationEquipmentInfo.builder()
                .equipmentName("오리발")
                .rentNumber(5)
                .size("270")
                .build();
        reservationEquipmentInfos.add(reservationEquipmentInfo);

        ReservationInfo reservationInfo = ReservationInfo.builder()
                .reservationId(1L)
                .studentId(1L)
                .studentNickname("열혈다이버")
                .studentNumber(5)
                .reservationEquipmentInfoList(reservationEquipmentInfos)
                .build();

        given(scheduleService.findReservationForSchedule(any(), any())).willReturn(List.of(reservationInfo));

        mockMvc.perform(get("/schedule/reservation-info")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("scheduleId", String.valueOf(scheduleId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("schedule-read-reservation-info",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("scheduleId").description("강의 일정 식별자 값")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.reservationInfoList[].reservationId").description("예약 식별자 값"),
                                        fieldWithPath("_embedded.reservationInfoList[].studentId").description("수강생 식별자 값"),
                                        fieldWithPath("_embedded.reservationInfoList[].studentNickname").description("수강생 닉네임"),
                                        fieldWithPath("_embedded.reservationInfoList[].studentNumber").description("예약한 수강생 수"),
                                        fieldWithPath("_embedded.reservationInfoList[].reservationEquipmentInfoList[].equipmentName").description("대여 장비 이름"),
                                        fieldWithPath("_embedded.reservationInfoList[].reservationEquipmentInfoList[].size").description("대여 장비 사이즈"),
                                        fieldWithPath("_embedded.reservationInfoList[].reservationEquipmentInfoList[].rentNumber").description("대여 장비 수"),
                                        fieldWithPath("_links.self.href").description("해당 API 주소"),
                                        fieldWithPath("_links.profile.href").description("해당 API 문서 주소")
                                )
                        )
                );
    }

    @Test
    @DisplayName("일정 삭제")
    public void cancelSchedule() throws Exception {
        Long scheduleId = 1L;

        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        mockMvc.perform(delete("/schedule/{id}", scheduleId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("schedule-delete",
                                pathParameters(
                                        parameterWithName("id").description("일정 식별자 값")
                                ),
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                )
                        )
                );
    }
}