package com.diving.pungdong.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ReservationInfo {
    private String userName;
    private List<String> equipmentList;
    private String description;
}
