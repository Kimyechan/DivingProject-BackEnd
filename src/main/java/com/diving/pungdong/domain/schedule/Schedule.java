package com.diving.pungdong.domain.schedule;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id @GeneratedValue
    private Long id;

    private Integer period;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY)
    private List<ScheduleDetail> scheduleDetails;
}
