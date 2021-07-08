package com.diving.pungdong.service.kafka.dto.reservation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationCreateInfo {
    private String title;
    private String body;
    private List<String> applicantIds;
    private String lectureId;
}
