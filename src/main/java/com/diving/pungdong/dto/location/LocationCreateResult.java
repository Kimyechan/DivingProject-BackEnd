package com.diving.pungdong.dto.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LocationCreateResult {
    private Long lectureId;
    private Long locationId;
}
