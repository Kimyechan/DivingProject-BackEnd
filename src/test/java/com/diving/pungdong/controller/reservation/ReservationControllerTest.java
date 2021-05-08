package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationDate;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.dto.reservation.ReservationInfo;
import com.diving.pungdong.dto.reservation.ReservationSubInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleTimeInfo;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
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
@Import(RestDocsConfiguration.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private LectureService lectureService;

    public Account createAccount(Role role) {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .userName("yechan")
                .age(27)
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
    @DisplayName("강의 예약")
    public void createReservation() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<String> equipmentList = createEquipmentNameList();

        List<ReservationDateDto> reservationDateDtoList = createReservationDateDtoList();

        ReservationCreateReq req = ReservationCreateReq.builder()
                .scheduleId(1L)
                .description("발 사이즈 260, 옷 사이즈 L")
                .equipmentList(equipmentList)
                .reservationDateList(reservationDateDtoList)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .schedule(Schedule.builder().id(1L).build())
                .account(account)
                .build();

        given(reservationService.makeReservation(any(), any())).willReturn(reservation);

        mockMvc.perform(post("/reservation")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("reservation-create",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName("Authorization").description("access token 값"),
                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                        ),
                        requestFields(
                                fieldWithPath("scheduleId").description("강의 일정 식별자 값"),
                                fieldWithPath("reservationDateList[].scheduleDetailId").description("강의 상세 정보 식별자 값"),
                                fieldWithPath("reservationDateList[].scheduleTimeId").description("강의 시간 정보 식별자 값"),
                                fieldWithPath("reservationDateList[].date").description("예약 날짜"),
                                fieldWithPath("reservationDateList[].time").description("예약 시간"),
                                fieldWithPath("equipmentList[]").description("대여 장비 이름 리스트"),
                                fieldWithPath("description").description("대여 장비 관련 사이즈 정보 및 요청 사항")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                        ),
                        responseFields(
                                fieldWithPath("reservationId").description("강의 예약 식별자"),
                                fieldWithPath("scheduleId").description("강의 예약 일정 식별자"),
                                fieldWithPath("accountId").description("예약 수강생 식별자"),
                                fieldWithPath("_links.self.href").description("해당 API URL")
                        )
                ));
    }

    public List<ReservationDateDto> createReservationDateDtoList() {
        List<ReservationDateDto> reservationDateDtoList = new ArrayList<>();

        ReservationDateDto reservationDateDto = ReservationDateDto.builder()
                .scheduleDetailId(1L)
                .scheduleTimeId(1L)
                .date(LocalDate.of(2021, 4, 20))
                .time(LocalTime.of(14, 0))
                .build();
        reservationDateDtoList.add(reservationDateDto);
        return reservationDateDtoList;
    }

    public List<String> createEquipmentNameList() {
        List<String> equipmentList = new ArrayList<>();
        equipmentList.add("오리발");
        equipmentList.add("슈트");
        return equipmentList;
    }

    @Test
    @DisplayName("수강생의 예약 리스트 검색")
    public void searchReservationList() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<ReservationSubInfo> reservationSubInfoList = new ArrayList<>();
        ReservationSubInfo reservationSubInfo = ReservationSubInfo.builder()
                .reservationId(1L)
                .lectureTitle("프리 다이빙 강의 1")
                .isMultipleCourse(false)
                .totalCost(100000)
                .dateOfReservation(LocalDate.of(2021, 3, 4))
                .build();
        reservationSubInfoList.add(reservationSubInfo);
        Pageable pageable = PageRequest.of(0, 5);
        Page<ReservationSubInfo> reservationSubInfoPage = new PageImpl<>(reservationSubInfoList, pageable, reservationSubInfoList.size());

        given(reservationService.findMyReservationList(account.getId(), pageable)).willReturn(reservationSubInfoPage);

        mockMvc.perform(get("/reservation/list")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-get-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName("Authorization").description("access token 값"),
                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("한 페이지당 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.reservationSubInfoList[].reservationId").description("예약 식별자 값"),
                                fieldWithPath("_embedded.reservationSubInfoList[].lectureTitle").description("예약 강의 제목"),
                                fieldWithPath("_embedded.reservationSubInfoList[].totalCost").description("예약 비용"),
                                fieldWithPath("_embedded.reservationSubInfoList[].isMultipleCourse").description("다회차 인지 아닌지의 여부"),
                                fieldWithPath("_embedded.reservationSubInfoList[].dateOfReservation").description("예약 날짜"),
                                fieldWithPath("_links.self.href").description("해당 API URL"),
                                fieldWithPath("page.size").description("한 페이지당 크기"),
                                fieldWithPath("page.totalElements").description("전체 요소 갯수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                fieldWithPath("page.number").description("현재 페이지 번호")
                        )
                ));
    }

    @Test
    @DisplayName("예약 상세 조회")
    public void getReservationDetail() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        Long reservationId = 1L;

        Location location = Location.builder()
                .latitude(36.568)
                .longitude(137.546)
                .address("서울시 잠실 수영장")
                .build();

        ReservationDate reservationDate = ReservationDate.builder()
                .date(LocalDate.of(2021, 3, 4))
                .time(LocalTime.of(18, 0))
                .scheduleDetail(ScheduleDetail.builder().location(location).build())
                .build();

        Reservation reservation = Reservation.builder()
                .account(account)
                .reservationDateList(List.of(reservationDate))
                .equipmentList(List.of("오리발", "슈트"))
                .description("오리발 270, 슈트 L")
                .build();

        given(reservationService.getDetailById(reservationId)).willReturn(reservation);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/reservation/{id}", reservationId)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-get-detail",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName("Authorization").description("access token 값"),
                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                        ),
                        pathParameters(
                                parameterWithName("id").description("예약 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("reservationScheduleList[].date").description("강의 날짜"),
                                fieldWithPath("reservationScheduleList[].time").description("강의 시간"),
                                fieldWithPath("reservationScheduleList[].location.latitude").description("강의 위치 위도"),
                                fieldWithPath("reservationScheduleList[].location.longitude").description("강의 위치 경도"),
                                fieldWithPath("reservationScheduleList[].location.address").description("강의 위치 주소"),
                                fieldWithPath("equipmentNameList[]").description("대여 장비 이름"),
                                fieldWithPath("description").description("대여 장비 사이즈 및 요청사항"),
                                fieldWithPath("_links.self.href").description("해당 API URL")
                        )
                ));
    }

    @Test
    @DisplayName("강의 예약 취소")
    public void cancelReservation() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));
        Long reservationId = 1L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .build();

        given(reservationService.getDetailById(reservationId)).willReturn(reservation);

        mockMvc.perform(delete("/reservation/{id}", reservationId)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-delete",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName("Authorization").description("access token 값"),
                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                        ),
                        pathParameters(
                                parameterWithName("id").description("예약 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("reservationCancelId").description("취소된 예약 식별자 값"),
                                fieldWithPath("success").description("예약취소 성공 여부")
                        )
                ));
    }

    @Test
    @DisplayName("일정의 한 타임에 예약 정보 조회")
    public void getReservationInfoForSchedule() throws Exception {
        Account account = createAccount(Role.INSTRUCTOR);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.INSTRUCTOR));
        ScheduleTimeInfo scheduleTimeInfo = ScheduleTimeInfo.builder()
                .lectureId(1L)
                .scheduleTimeId(1L)
                .build();

        ReservationInfo reservationInfo = ReservationInfo.builder()
                .userName("홍길동")
                .equipmentList(List.of("오리발", "슈트"))
                .description("오리발 사이즈 260, 슈트 사이즈 L")
                .build();
        List<ReservationInfo> reservationInfos = new ArrayList<>();
        reservationInfos.add(reservationInfo);

        doNothing().when(lectureService).checkRightInstructor(account, scheduleTimeInfo.getLectureId());
        given(reservationService.getReservationForSchedule(scheduleTimeInfo.getScheduleTimeId())).willReturn(reservationInfos);

        mockMvc.perform(get("/reservation/students")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .content(objectMapper.writeValueAsString(scheduleTimeInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-get-list-for-schedule",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName("Authorization").description("access token 값"),
                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
                        ),
                        requestFields(
                                fieldWithPath("lectureId").description("강의 정보 식별자 값"),
                                fieldWithPath("scheduleTimeId").description("강의 시간 정보 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.reservationInfoList[].userName").description("예약한 수강생 이름"),
                                fieldWithPath("_embedded.reservationInfoList[].equipmentList[]").description("예약한 수강생 대여 장비 목록"),
                                fieldWithPath("_embedded.reservationInfoList[].description").description("예약한 수강생 대여 장비 사이즈 설명 및 요청사항")
                        )
                ));
    }
}