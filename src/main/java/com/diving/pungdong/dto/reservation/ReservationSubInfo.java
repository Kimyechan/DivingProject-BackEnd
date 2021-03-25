package com.diving.pungdong.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class ReservationSubInfo {
    private Long reservationId;
    private String lectureTitle;
    private Integer totalCost;
    private Boolean isMultipleCourse;
    private LocalDate dateOfReservation;
}
