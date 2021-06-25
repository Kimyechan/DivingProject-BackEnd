package com.diving.pungdong.controller.reservation;


import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.controller.lecture.LectureController;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.lecture.detail.LectureDetail;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.dto.reservation.ReservationResult;
import com.diving.pungdong.dto.reservation.detail.ReservationDetail;
import com.diving.pungdong.dto.reservation.list.ReservationInfo;
import com.diving.pungdong.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reservation", produces = MediaTypes.HAL_JSON_VALUE)
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<?> create(@CurrentUser Account account,
                                    @RequestBody ReservationCreateInfo reservationCreateInfo,
                                    BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        Reservation reservation = reservationService.saveReservation(account, reservationCreateInfo);

        ReservationResult reservationResult = new ReservationResult(reservation.getId());
        EntityModel<ReservationResult> model = EntityModel.of(reservationResult);
        WebMvcLinkBuilder selfLink = linkTo(methodOn(ReservationController.class).create(account, reservationCreateInfo, result));
        model.add(selfLink.withSelfRel());

        return ResponseEntity.created(selfLink.toUri()).body(model);
    }

    @GetMapping("/list")
    public ResponseEntity<?> readMyReservations(@CurrentUser Account account,
                                                Pageable pageable,
                                                PagedResourcesAssembler<ReservationInfo> assembler) {
        Page<ReservationInfo> reservationInfoPage = reservationService.findMyReservations(account, pageable);

        PagedModel<EntityModel<ReservationInfo>> model = assembler.toModel(reservationInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping
    public ResponseEntity<?> readReservationDetail(@CurrentUser Account account,
                                                   @RequestParam Long reservationId) {
        ReservationDetail reservationDetail = reservationService.findMyReservationDetail(account, reservationId);

        EntityModel<ReservationDetail> model = EntityModel.of(reservationDetail);
        model.add(linkTo(methodOn(ReservationController.class).readReservationDetail(account, reservationId)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-reservation-read").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }
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
