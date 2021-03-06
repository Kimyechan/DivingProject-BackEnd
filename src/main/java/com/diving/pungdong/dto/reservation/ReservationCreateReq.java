package com.diving.pungdong.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateReq {
    private Long scheduleId;
    private List<ReservationDateDto> reservationDateList;
    private List<String> equipmentList;
    private String description;
}
