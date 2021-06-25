package com.diving.pungdong.dto.reservation.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationDetail {
    private Long reservationId;
    private LocalDate dateOfReservation;
    private Integer numberOfPeople;
    private PaymentDetail paymentDetail;
}
