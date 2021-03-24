package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import com.diving.pungdong.dto.reservation.ReservationSubInfo;
import com.diving.pungdong.service.AccountService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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

    public Account createAccount() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .userName("yechan")
                .age(27)
                .gender(Gender.MALE)
                .roles(Set.of(Role.STUDENT))
                .build();

        given(accountService.loadUserByUsername(String.valueOf(account.getId())))
                .willReturn(new User(account.getEmail(), account.getPassword(), authorities(account.getRoles())));

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
        Account account = createAccount();
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
        Account account = createAccount();
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<ReservationSubInfo> reservationSubInfoList = new ArrayList<>();
        ReservationSubInfo reservationSubInfo = ReservationSubInfo.builder()
                .lectureTitle("프리 다이빙 강의 1")
                .isMultipleCourse(false)
                .totalCost(100000)
                .dateOfReservation(LocalDate.of(2021, 3, 4))
                .build();
        reservationSubInfoList.add(reservationSubInfo);
        Pageable pageable = PageRequest.of(0, 5);
        Page<ReservationSubInfo> reservationSubInfoPage = new PageImpl<>(reservationSubInfoList, pageable, reservationSubInfoList.size());

        given(reservationService.findMyReservationList(account.getEmail(), pageable)).willReturn(reservationSubInfoPage);

        mockMvc.perform(get("/reservation/list")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header("IsRefreshToken", "false")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk());
    }
}