package com.diving.pungdong.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetail {
    private List<ReservationSchedule> reservationScheduleList;
    private List<String> equipmentNameList;
    private String description;
}
