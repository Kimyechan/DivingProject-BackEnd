package com.diving.pungdong.dto.reservation;

import com.diving.pungdong.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDateDto {
    private LocalDate date;
    private LocalTime time;
    private Location location;
}
