package com.diving.pungdong.dto.reservation.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationInfo {
    private Long reservationId;
    private String lectureTitle;
    private String lectureImageUrl;
    private String instructorNickname;
    private LocalDate reservationDate;
    private Long remainingDate;
}
