package com.diving.pungdong.controller.reservation;

import com.diving.pungdong.dto.reservation.ReservationCreateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reservation", produces = MediaTypes.HAL_JSON_VALUE)
public class ReservationController {

    @PostMapping
    public ResponseEntity<?> create(Authentication authentication, @RequestBody ReservationCreateReq req) {

        return ResponseEntity.ok().build();
    }
}
