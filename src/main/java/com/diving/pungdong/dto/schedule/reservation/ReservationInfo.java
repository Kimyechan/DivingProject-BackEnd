package com.diving.pungdong.dto.schedule.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ReservationInfo {
    private Long reservationId;
    private Long studentId;
    private String studentNickname;
    private Integer studentNumber;
    private List<ReservationEquipmentInfo> reservationEquipmentInfoList;
}
