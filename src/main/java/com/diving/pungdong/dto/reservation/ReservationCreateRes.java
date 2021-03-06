package com.diving.pungdong.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRes {
    private Long reservationId;
    private Long scheduleId;
    private Long accountId;
}
