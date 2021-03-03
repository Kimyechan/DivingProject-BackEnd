package com.diving.pungdong.domain.schedule;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTime {
    @Id @GeneratedValue
    private Long id;

    private Integer currentNumber;

    private LocalTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleDetail scheduleDetail;
}
