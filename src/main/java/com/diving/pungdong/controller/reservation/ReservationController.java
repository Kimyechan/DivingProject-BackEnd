package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationCreateRes;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
