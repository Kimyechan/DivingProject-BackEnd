package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.reservation.RentEquipmentInfo;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.dto.reservation.detail.PaymentDetail;
import com.diving.pungdong.dto.reservation.detail.ReservationDetail;
import com.diving.pungdong.dto.reservation.detail.ScheduleDetail;
import com.diving.pungdong.dto.reservation.list.ReservationInfo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.reservation.ReservationService;
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
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    private ScheduleService scheduleService;

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
    @DisplayName("강의 예약")
    public void createReservation() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<RentEquipmentInfo> rentEquipmentInfos = new ArrayList<>();
        RentEquipmentInfo equipmentInfo = RentEquipmentInfo.builder()
                .scheduleEquipmentStockId(1L)
                .rentNumber(4)
                .build();
        rentEquipmentInfos.add(equipmentInfo);

        ReservationCreateInfo reservationCreateInfo = ReservationCreateInfo.builder()
                .scheduleId(1L)
                .numberOfPeople(4)
                .rentEquipmentInfos(rentEquipmentInfos)
                .build();

        given(reservationService.saveReservation(any(), any())).willReturn(Reservation.builder().id(1L).build());

        mockMvc.perform(post("/reservation")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(reservationCreateInfo)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("reservation-create",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestFields(
                                fieldWithPath("scheduleId").description("강의 일정 식별자 값"),
                                fieldWithPath("numberOfPeople").description("강의 예약 인원 수"),
                                fieldWithPath("rentEquipmentInfos[].scheduleEquipmentStockId").description("대여 장비 재고 식별자 값"),
                                fieldWithPath("rentEquipmentInfos[].rentNumber").description("대여 장비 수")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL JSON 타입")
                        ),
                        responseFields(
                                fieldWithPath("reservationId").description("강의 예약 식별자"),
                                fieldWithPath("_links.self.href").description("해당 API URL")
                        )
                ));
    }

    @Test
    @DisplayName("내 강의 예약 목록 보기")
    public void readMyReservations() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<ReservationInfo> reservationInfos = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            ReservationInfo reservationInfo = ReservationInfo.builder()
                    .reservationId((long) i)
                    .remainingDate((long) i)
                    .reservationDate(LocalDate.now().minusDays(i + 1))
                    .lectureTitle("강의 타이틀")
                    .lectureImageUrl("강의 이미지 Url")
                    .instructorNickname("강사 닉네임")
                    .build();
            reservationInfos.add(reservationInfo);
        }
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "dateOfReservation"));
        Page<ReservationInfo> reservationInfoPage = new PageImpl<>(reservationInfos, pageable, reservationInfos.size());

        given(reservationService.findMyReservations(any(), any())).willReturn(reservationInfoPage);

        mockMvc.perform(get("/reservation/list")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .param("sort", String.valueOf(pageable.getSort())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "reservation-find-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기"),
                                        parameterWithName("sort").description("정렬 기준")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.reservationInfoList[].reservationId").description("예약 식별자 값"),
                                        fieldWithPath("_embedded.reservationInfoList[].lectureTitle").description("강의 타이틀"),
                                        fieldWithPath("_embedded.reservationInfoList[].lectureImageUrl").description("강의 이미지 Url"),
                                        fieldWithPath("_embedded.reservationInfoList[].instructorNickname").description("강사 닉네임"),
                                        fieldWithPath("_embedded.reservationInfoList[].reservationDate").description("예약 날짜"),
                                        fieldWithPath("_embedded.reservationInfoList[].remainingDate").description("예약 최근 일정 남은 날짜"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("page.size").description("한 페이지 당 사이즈"),
                                        fieldWithPath("page.totalElements").description("전체 신규 강의 갯수"),
                                        fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                        fieldWithPath("page.number").description("현재 페이지 번호")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 예약 정보 조회")
    public void readReservationDetail() throws Exception {
        Long reservationId = 1L;

        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        PaymentDetail paymentDetail = PaymentDetail.builder()
                .lectureCost(100000)
                .equipmentRentCost(20000)
                .build();

        ReservationDetail reservationDetail = ReservationDetail.builder()
                .reservationId(1L)
                .dateOfReservation(LocalDate.of(2021, 7, 11))
                .numberOfPeople(5)
                .paymentDetail(paymentDetail)
                .build();

        given(reservationService.findMyReservationDetail(any(), any())).willReturn(reservationDetail);

        mockMvc.perform(get("/reservation")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("reservationId", String.valueOf(reservationId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-read",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestParameters(
                                parameterWithName("reservationId").description("강의 예약 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("reservationId").description("강의 예약 식별자"),
                                fieldWithPath("dateOfReservation").description("강의 예약 날짜"),
                                fieldWithPath("numberOfPeople").description("강의 예약 인원"),
                                fieldWithPath("paymentDetail.lectureCost").description("강의 비용"),
                                fieldWithPath("paymentDetail.equipmentRentCost").description("대여 장비 비용"),
                                fieldWithPath("_links.self.href").description("해당 API URL"),
                                fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                        )
                ));
    }

    @Test
    @DisplayName("강의 예약 일정 조회")
    public void readReservationSchedule() throws Exception {
        Long reservationId = 1L;

        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<ScheduleDetail> scheduleDetails = new ArrayList<>();
        ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                .date(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(12, 30))
                .build();
        scheduleDetails.add(scheduleDetail);

        given(scheduleService.findByReservationId(any())).willReturn(scheduleDetails);

        mockMvc.perform(get("/reservation/schedule")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("reservationId", String.valueOf(reservationId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-read-schedule-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestParameters(
                                parameterWithName("reservationId").description("강의 예약 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.scheduleDetailList[].startTime").description("강의 시작 시간"),
                                fieldWithPath("_embedded.scheduleDetailList[].endTime").description("강의 종료 시간"),
                                fieldWithPath("_embedded.scheduleDetailList[].date").description("강의 일정 한 날짜"),
                                fieldWithPath("_links.self.href").description("해당 API URL"),
                                fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                        )
                ));
    }
//
//    @Test
//    @DisplayName("강의 예약 취소")
//    public void cancelReservation() throws Exception {
//        Account account = createAccount(Role.STUDENT);
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));
//        Long reservationId = 1L;
//
//        Reservation reservation = Reservation.builder()
//                .id(reservationId)
//                .build();
//
//        given(reservationService.getDetailById(reservationId)).willReturn(reservation);
//
//        mockMvc.perform(delete("/reservation/{id}", reservationId)
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("reservation-delete",
//                        requestHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
//                                headerWithName("Authorization").description("access token 값"),
//                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
//                        ),
//                        pathParameters(
//                                parameterWithName("id").description("예약 식별자 값")
//                        ),
//                        responseFields(
//                                fieldWithPath("reservationCancelId").description("취소된 예약 식별자 값"),
//                                fieldWithPath("success").description("예약취소 성공 여부")
//                        )
//                ));
//    }
//
//    @Test
//    @DisplayName("일정의 한 타임에 예약 정보 조회")
//    public void getReservationInfoForSchedule() throws Exception {
//        Account account = createAccount(Role.INSTRUCTOR);
//        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.INSTRUCTOR));
//        ScheduleTimeInfo scheduleTimeInfo = ScheduleTimeInfo.builder()
//                .lectureId(1L)
//                .scheduleTimeId(1L)
//                .build();
//
//        ReservationInfo reservationInfo = ReservationInfo.builder()
//                .userName("홍길동")
//                .equipmentList(List.of("오리발", "슈트"))
//                .description("오리발 사이즈 260, 슈트 사이즈 L")
//                .build();
//        List<ReservationInfo> reservationInfos = new ArrayList<>();
//        reservationInfos.add(reservationInfo);
//
//        doNothing().when(lectureService).checkRightInstructor(account, scheduleTimeInfo.getLectureId());
//        given(reservationService.getReservationForSchedule(scheduleTimeInfo.getScheduleTimeId())).willReturn(reservationInfos);
//
//        mockMvc.perform(get("/reservation/students")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, accessToken)
//                .header("IsRefreshToken", "false")
//                .content(objectMapper.writeValueAsString(scheduleTimeInfo)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andDo(document("reservation-get-list-for-schedule",
//                        requestHeaders(
//                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
//                                headerWithName("Authorization").description("access token 값"),
//                                headerWithName("IsRefreshToken").description("token이 refresh token인지 확인")
//                        ),
//                        requestFields(
//                                fieldWithPath("lectureId").description("강의 정보 식별자 값"),
//                                fieldWithPath("scheduleTimeId").description("강의 시간 정보 식별자 값")
//                        ),
//                        responseFields(
//                                fieldWithPath("_embedded.reservationInfoList[].userName").description("예약한 수강생 이름"),
//                                fieldWithPath("_embedded.reservationInfoList[].equipmentList[]").description("예약한 수강생 대여 장비 목록"),
//                                fieldWithPath("_embedded.reservationInfoList[].description").description("예약한 수강생 대여 장비 사이즈 설명 및 요청사항")
//                        )
//                ));
//    }
}