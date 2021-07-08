package com.diving.pungdong.service.kafka.dto.reservation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationCreateInfo {
    private String instructorId;
    private String lectureId;
    private String scheduleId;
    private String messageBody;
}
