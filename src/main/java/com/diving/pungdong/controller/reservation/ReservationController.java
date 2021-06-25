package com.diving.pungdong.controller.reservation;


import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.reservation.ReservationCreateInfo;
import com.diving.pungdong.dto.reservation.ReservationResult;
import com.diving.pungdong.dto.reservation.detail.LocationDetail;
import com.diving.pungdong.dto.reservation.detail.ReservationDetail;
import com.diving.pungdong.dto.reservation.detail.ScheduleDetail;
import com.diving.pungdong.dto.reservation.list.ReservationInfo;
import com.diving.pungdong.dto.schedule.read.ScheduleInfo;
import com.diving.pungdong.service.LocationService;
import com.diving.pungdong.service.reservation.ReservationService;
import com.diving.pungdong.service.schedule.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reservation", produces = MediaTypes.HAL_JSON_VALUE)
public class ReservationController {
    private final ReservationService reservationService;
    private final ScheduleService scheduleService;
    private final LocationService locationService;

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

    @GetMapping("/schedule")
    public ResponseEntity<?> readReservationSchedule(@RequestParam Long reservationId) {
        List<ScheduleDetail> scheduleDetails = scheduleService.findByReservationId(reservationId);

        CollectionModel<ScheduleDetail> model = CollectionModel.of(scheduleDetails);
        model.add(linkTo(methodOn(ReservationController.class).readReservationSchedule(reservationId)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-reservation-read-schedule-list").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/location")
    public ResponseEntity<?> readReservationLectureLocation(@RequestParam Long reservationId) {
        LocationDetail locationDetail = locationService.findByReservationId(reservationId);

        EntityModel<LocationDetail> model = EntityModel.of(locationDetail);
        model.add(linkTo(methodOn(ReservationController.class).readReservationLectureLocation(reservationId)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-reservation-read-lecture-location").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

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
