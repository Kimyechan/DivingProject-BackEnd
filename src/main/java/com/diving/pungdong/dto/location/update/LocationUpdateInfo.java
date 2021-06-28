package com.diving.pungdong.dto.location.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationUpdateInfo {
    @NotNull
    private Long lectureId;

    @NotEmpty
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
