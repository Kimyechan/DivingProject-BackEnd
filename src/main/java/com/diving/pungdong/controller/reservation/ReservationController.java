package com.diving.pungdong.controller.reservation;


import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.reservation.ReservationService;
import com.diving.pungdong.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reservation", produces = MediaTypes.HAL_JSON_VALUE)
public class ReservationController {
    private final ReservationService reservationService;
    private final AccountService accountService;
    private final LectureService lectureService;

    @PostMapping
    public ResponseEntity<?> create(@CurrentUser Account account,
                                    @RequestBody ReservationCreateInfo reservationCreateInfo) {
        Reservation reservation = reservationService.saveReservation(account, reservationCreateInfo);
        return ResponseEntity.created(null).body(null);
    }
//
//    @GetMapping("/list")
//    public ResponseEntity<?> getList(@CurrentUser Account account,
//                                     Pageable pageable,
//                                     PagedResourcesAssembler<ReservationSubInfo> assembler) {
//        Page<ReservationSubInfo> reservationSubInfoPage = reservationService.findMyReservationList(account.getId(), pageable);
//
//        PagedModel<EntityModel<ReservationSubInfo>> models = assembler.toModel(reservationSubInfoPage);
//        return ResponseEntity.ok().body(models);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getDetail(Authentication authentication, @PathVariable("id") Long id) {
//        Reservation reservation = reservationService.getDetailById(id);
//        reservationService.checkRightForReservation(authentication.getName(), reservation);
//
//        ReservationDetail reservationDetail = mapToReservationDetail(reservation);
//
//        EntityModel<ReservationDetail> model = EntityModel.of(reservationDetail);
//        model.add(linkTo(methodOn(ReservationController.class).getDetail(authentication, id)).withSelfRel());
//
//        return ResponseEntity.ok().body(model);
//    }
//
//    private ReservationDetail mapToReservationDetail(Reservation reservation) {
//        List<ReservationDate> reservationDates = reservation.getReservationDateList();
//        List<ReservationSchedule> reservationSchedules = mapToReservationSchedules(reservationDates);
//
//        return ReservationDetail.builder()
//                .reservationScheduleList(reservationSchedules)
//                .equipmentNameList(reservation.getEquipmentList())
//                .description(reservation.getDescription())
//                .build();
//    }
//
//    private List<ReservationSchedule> mapToReservationSchedules(List<ReservationDate> reservationDates) {
//        List<ReservationSchedule> reservationSchedules = new ArrayList<>();
//
//        for (ReservationDate reservationDate : reservationDates) {
//            ReservationSchedule reservationSchedule = mapToReservationSchedule(reservationDate);
//            reservationSchedules.add(reservationSchedule);
//        }
//
//        return reservationSchedules;
//    }
//
//    private ReservationSchedule mapToReservationSchedule(ReservationDate reservationDate) {
//        return ReservationSchedule.builder()
//                .date(reservationDate.getDate())
//                .time(reservationDate.getTime())
//                .location(reservationDate.getScheduleDate().getLocation())
//                .build();
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> cancel(Authentication authentication, @PathVariable("id") Long id) {
//        Reservation reservation = reservationService.getDetailById(id);
//        reservationService.checkRightForReservation(authentication.getName(), reservation);
//
//        reservationService.cancelReservation(id);
//
//        ReservationCancelRes reservationCancelRes = mapToReservationCancelRes(id);
//
//        return ResponseEntity.ok().body(reservationCancelRes);
//    }
//
//    public ReservationCancelRes mapToReservationCancelRes(Long id) {
//        return ReservationCancelRes.builder()
//                .reservationCancelId(id)
//                .success(true)
//                .build();
//    }
//
//    @GetMapping("/students")
//    public ResponseEntity<?> getStudents(@CurrentUser Account account,
//                                         @RequestBody ScheduleTimeInfo scheduleTimeInfo) {
//        lectureService.checkRightInstructor(account, scheduleTimeInfo.getLectureId());
//        List<ReservationInfo> reservationInfos = reservationService.getReservationForSchedule(scheduleTimeInfo.getScheduleTimeId());
//
//        CollectionModel<ReservationInfo> models = CollectionModel.of(reservationInfos);
//
//        return ResponseEntity.ok().body(models);
//    }

}
