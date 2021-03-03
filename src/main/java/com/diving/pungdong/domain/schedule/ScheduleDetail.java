package com.diving.pungdong.domain.schedule;

import com.diving.pungdong.domain.Location;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetail {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDate date;

    private LocalTime lectureTime;

    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;

    @OneToMany(mappedBy = "scheduleDetail", fetch = FetchType.LAZY)
    private List<ScheduleTime> scheduleTimes;
}
