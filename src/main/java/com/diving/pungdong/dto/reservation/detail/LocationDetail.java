package com.diving.pungdong.dto.reservation.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LocationDetail {
    private String address;
    private Double latitude;
    private Double longitude;
}
