package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.config.RestDocsConfiguration;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.reservation.RentEquipmentInfo;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.dto.reservation.detail.*;
import com.diving.pungdong.dto.reservation.list.FutureReservationUIModel;
import com.diving.pungdong.dto.reservation.list.PastReservationUIModel;
import com.diving.pungdong.dto.reservation.list.ReservationInfo;
import com.diving.pungdong.dto.schedule.notification.Notification;
import com.diving.pungdong.service.LocationService;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    private ScheduleService scheduleService;

    @MockBean
    private LocationService locationService;

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
                    .organization(Organization.AIDA)
                    .level("Level1")
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
                                        fieldWithPath("_embedded.reservationInfoList[].organization").description("강의 자격증 소속 단체"),
                                        fieldWithPath("_embedded.reservationInfoList[].level").description("강의 자격증 레벨"),
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
    @DisplayName("앞으로 진행될 강의 예약 목록 읽기")
    public void readMyFutureReservations() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<FutureReservationUIModel> futureReservations = createFutureReservations();

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "dateOfReservation"));
        Page<FutureReservationUIModel> futureReservationPage = new PageImpl<>(futureReservations, pageable, futureReservations.size());

        given(reservationService.findMyFutureReservations(any(), any())).willReturn(futureReservationPage);

        mockMvc.perform(get("/reservation/future")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .param("sort", String.valueOf(pageable.getSort())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "reservation-find-future-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기"),
                                        parameterWithName("sort").description("정렬 기준")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.futureReservationUIModelList[].reservationId").description("예약 식별자 값"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].lectureTitle").description("강의 타이틀"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].organization").description("강의 자격증 소속 단체"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].level").description("강의 자격증 레벨"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].lectureImageUrl").description("강의 이미지 Url"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].instructorNickname").description("강사 닉네임"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].reservationDate").description("예약 날짜"),
                                        fieldWithPath("_embedded.futureReservationUIModelList[].remainingDate").description("예약 최근 일정 남은 날짜"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("page.size").description("한 페이지 당 사이즈"),
                                        fieldWithPath("page.totalElements").description("전체 신규 강의 갯수"),
                                        fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                        fieldWithPath("page.number").description("현재 페이지 번호")
                                )
                        )
                );
    }

    private List<FutureReservationUIModel> createFutureReservations() {
        List<FutureReservationUIModel> futureReservations = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Reservation reservation = Reservation.builder()
                    .id((long) i)
                    .dateOfReservation(LocalDate.of(2021, 3, 21))
                    .build();

            Lecture lecture = Lecture.builder()
                    .title("강의 제목")
                    .organization(Organization.AIDA)
                    .level("Level 1")
                    .build();

            FutureReservationUIModel futureReservation = FutureReservationUIModel.builder()
                    .reservation(reservation)
                    .lecture(lecture)
                    .lectureImageUrl("강의 대표 이미지 링크")
                    .instructorNickname("강사 닉네임")
                    .remainingDate(10L)
                    .build();

            futureReservations.add(futureReservation);
        }

        return futureReservations;
    }

    @Test
    @DisplayName("진행이 완료된 지난 강의 예약 목록 읽기")
    public void readMyPastReservations() throws Exception {
        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<PastReservationUIModel> pastReservationUIModels = createPastReservations();

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "dateOfReservation"));
        Page<PastReservationUIModel> pastReservationPage = new PageImpl<>(pastReservationUIModels, pageable, pastReservationUIModels.size());

        given(reservationService.findMyPastReservation(any(), any())).willReturn(pastReservationPage);

        mockMvc.perform(get("/reservation/past")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
                .param("sort", String.valueOf(pageable.getSort())))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "reservation-find-past-list",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("몇 번째 페이지"),
                                        parameterWithName("size").description("한 페이지 당 크기"),
                                        parameterWithName("sort").description("정렬 기준")
                                ),
                                responseFields(
                                        fieldWithPath("_embedded.pastReservationUIModelList[].reservationId").description("예약 식별자 값"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].lectureTitle").description("강의 타이틀"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].organization").description("강의 자격증 소속 단체"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].level").description("강의 자격증 레벨"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].lectureImageUrl").description("강의 이미지 Url"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].instructorNickname").description("강사 닉네임"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].reservationDate").description("예약 날짜"),
                                        fieldWithPath("_embedded.pastReservationUIModelList[].isExistedReview").description("리뷰 작성 여부"),
                                        fieldWithPath("_links.self.href").description("해당 Api Url"),
                                        fieldWithPath("page.size").description("한 페이지 당 사이즈"),
                                        fieldWithPath("page.totalElements").description("전체 신규 강의 갯수"),
                                        fieldWithPath("page.totalPages").description("전체 페이지 갯수"),
                                        fieldWithPath("page.number").description("현재 페이지 번호")
                                )
                        )
                );
    }

    private List<PastReservationUIModel> createPastReservations() {
        List<PastReservationUIModel> pastReservationUIModels = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Reservation reservation = Reservation.builder()
                    .id((long) i)
                    .dateOfReservation(LocalDate.of(2021, 3, 21))
                    .build();

            Lecture lecture = Lecture.builder()
                    .title("강의 제목")
                    .organization(Organization.AIDA)
                    .level("Level 1")
                    .build();

            PastReservationUIModel pastReservation = PastReservationUIModel.builder()
                    .reservation(reservation)
                    .lecture(lecture)
                    .lectureImageUrl("강의 대표 이미지 링크")
                    .instructorNickname("강사 닉네임")
                    .isExistedReview(true)
                    .build();

            pastReservationUIModels.add(pastReservation);
        }

        return pastReservationUIModels;
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

        given(reservationService.findReservationScheduleDetail(any())).willReturn(scheduleDetails);

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

    @Test
    @DisplayName("예약한 강의 위치 조회")
    public void readReservationLectureLocation() throws Exception {
        Long reservationId = 1L;

        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        LocationDetail locationDetail = LocationDetail.builder()
                .address("위치 상세 주소")
                .longitude(36.13121231)
                .latitude(126.33124124)
                .build();

        given(locationService.findByReservationId(any())).willReturn(locationDetail);

        mockMvc.perform(get("/reservation/location")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("reservationId", String.valueOf(reservationId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-read-lecture-location",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestParameters(
                                parameterWithName("reservationId").description("강의 예약 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("address").description("예약한 강의 위치 상세 주소"),
                                fieldWithPath("latitude").description("위치 위도"),
                                fieldWithPath("longitude").description("위치 경도"),
                                fieldWithPath("_links.self.href").description("해당 API URL"),
                                fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                        )
                ));
    }

    @Test
    @DisplayName("예약한 강의 대여 장비 목록 조회")
    public void readReservationRentEquipments() throws Exception {
        Long reservationId = 1L;

        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        List<RentEquipmentDetail> rentEquipmentDetails = new ArrayList<>();
        RentEquipmentDetail rentEquipmentDetail = RentEquipmentDetail.builder()
                .equipmentName("오리발")
                .size("270")
                .rentNumber(2)
                .build();
        rentEquipmentDetails.add(rentEquipmentDetail);

        given(reservationService.findRentEquipments(any())).willReturn(rentEquipmentDetails);

        mockMvc.perform(get("/reservation/equipment/list")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("reservationId", String.valueOf(reservationId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("reservation-read-equipment-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestParameters(
                                parameterWithName("reservationId").description("강의 예약 식별자 값")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.rentEquipmentDetailList[].equipmentName").description("대여 장비 이름"),
                                fieldWithPath("_embedded.rentEquipmentDetailList[].size").description("대여 장비 크기"),
                                fieldWithPath("_embedded.rentEquipmentDetailList[].rentNumber").description("대여 장비 수"),
                                fieldWithPath("_links.self.href").description("해당 API URL"),
                                fieldWithPath("_links.profile.href").description("해당 Api 문서 Url")
                        )
                ));
    }

    @Test
    @DisplayName("강의 예약 취소")
    public void cancelReservation() throws Exception {
        Long reservationId = 1L;

        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());

        mockMvc.perform(delete("/reservation/{id}", reservationId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("reservation-delete",
                                pathParameters(
                                        parameterWithName("id").description("강의 예약 식별자")
                                ),
                                requestHeaders(
                                        headerWithName(org.apache.http.HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                        headerWithName(org.apache.http.HttpHeaders.AUTHORIZATION).optional().description("access token 값")
                                )
                        )
                );
    }

    @Test
    @DisplayName("강의 예약자에게 전달한 공지사항 생성")
    public void createNotification() throws Exception {
        Long scheduleId = 1L;

        Account account = createAccount(Role.STUDENT);
        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), Set.of(Role.STUDENT));

        Notification notification = Notification.builder()
                .title("공지사항 제목")
                .body("공지사항 본문")
                .build();

        mockMvc.perform(RestDocumentationRequestBuilders.post("/reservation/schedule/{id}/notification", scheduleId)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(notification)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("reservation-create-notification",
                        pathParameters(
                                parameterWithName("id").description("강의 일정 식별자 값")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("application json 타입"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token 값")
                        ),
                        requestFields(
                                fieldWithPath("title").description("공지사항 제목"),
                                fieldWithPath("body").description("공지사항 내용")
                        )
                ));
    }
}