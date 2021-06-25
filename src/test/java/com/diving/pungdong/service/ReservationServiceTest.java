package com.diving.pungdong.service;

import com.diving.pungdong.service.reservation.ReservationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Spy
    @InjectMocks
    private ReservationService reservationService;
}