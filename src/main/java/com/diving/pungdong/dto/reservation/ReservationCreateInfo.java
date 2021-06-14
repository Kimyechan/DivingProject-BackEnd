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
public class ReservationCreateInfo {
    private Long scheduleId;
    private Integer numberOfPeople;
    private List<RentEquipmentInfo> rentEquipmentInfos;
}
