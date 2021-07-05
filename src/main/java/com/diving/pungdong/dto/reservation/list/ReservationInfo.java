package com.diving.pungdong.dto.reservation.list;

import com.diving.pungdong.domain.lecture.Organization;
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
    private Organization organization;
    private String level;
    private String lectureImageUrl;
    private String instructorNickname;
    private LocalDate reservationDate;
    private Long remainingDate;
}
