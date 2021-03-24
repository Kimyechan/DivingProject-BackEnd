package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import com.diving.pungdong.dto.reservation.ReservationCreateRes;
import com.diving.pungdong.dto.reservation.ReservationSubInfo;
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

}
