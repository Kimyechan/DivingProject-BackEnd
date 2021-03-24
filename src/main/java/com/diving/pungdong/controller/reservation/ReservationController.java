package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.domain.reservation.ReservationDate;
import com.diving.pungdong.dto.reservation.*;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.naming.NoPermissionException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reservation", produces = MediaTypes.HAL_JSON_VALUE)
public class ReservationController {
    private final ReservationService reservationService;
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> create(Authentication authentication, @RequestBody ReservationCreateReq req) {
        Account account = accountService.findAccountByEmail(authentication.getName());
        Reservation reservation = reservationService.makeReservation(account, req);

        ReservationCreateRes res = mapToReservationCreateRes(reservation);

        WebMvcLinkBuilder selfLink = linkTo(methodOn(ReservationController.class).create(authentication, req));
        EntityModel<ReservationCreateRes> model = EntityModel.of(res);
        model.add(selfLink.withSelfRel());

        return ResponseEntity.created(selfLink.toUri()).body(model);
    }

    public ReservationCreateRes mapToReservationCreateRes(Reservation reservation) {
        return ReservationCreateRes.builder()
                .reservationId(reservation.getId())
                .accountId(reservation.getAccount().getId())
                .scheduleId(reservation.getSchedule().getId())
                .build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> getList(Authentication authentication,
                                     Pageable pageable,
                                     PagedResourcesAssembler<ReservationSubInfo> assembler) {
        String email = authentication.getName();
        Page<ReservationSubInfo> reservationSubInfoPage = reservationService.findMyReservationList(email, pageable);

        PagedModel<EntityModel<ReservationSubInfo>> models = assembler.toModel(reservationSubInfoPage);
        return ResponseEntity.ok().body(models);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(Authentication authentication, @PathVariable("id") Long id) {
        Reservation reservation = reservationService.getDetailById(id);
        reservationService.checkRightForReservation(authentication.getName(), reservation);

        ReservationDetail reservationDetail = mapToReservationDetail(reservation);

        EntityModel<ReservationDetail> model = EntityModel.of(reservationDetail);
        model.add(linkTo(methodOn(ReservationController.class).getDetail(authentication, id)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    private ReservationDetail mapToReservationDetail(Reservation reservation) {
        List<ReservationDate> reservationDates = reservation.getReservationDateList();
        List<ReservationSchedule> reservationSchedules = mapToReservationSchedules(reservationDates);

        return ReservationDetail.builder()
                .reservationScheduleList(reservationSchedules)
                .equipmentNameList(reservation.getEquipmentList())
                .description(reservation.getDescription())
                .build();
    }

    private List<ReservationSchedule> mapToReservationSchedules(List<ReservationDate> reservationDates) {
        List<ReservationSchedule> reservationSchedules = new ArrayList<>();

        for (ReservationDate reservationDate : reservationDates) {
            ReservationSchedule reservationSchedule = mapToReservationSchedule(reservationDate);
            reservationSchedules.add(reservationSchedule);
        }

        return reservationSchedules;
    }

    private ReservationSchedule mapToReservationSchedule(ReservationDate reservationDate) {
        return ReservationSchedule.builder()
                .date(reservationDate.getDate())
                .time(reservationDate.getTime())
                .location(reservationDate.getScheduleDetail().getLocation())
                .build();
    }

}
