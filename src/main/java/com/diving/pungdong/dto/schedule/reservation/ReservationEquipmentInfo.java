package com.diving.pungdong.dto.schedule.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReservationEquipmentInfo {
    private String equipmentName;
    private String size;
    private Integer rentNumber;
}
