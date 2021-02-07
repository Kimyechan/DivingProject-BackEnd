package com.diving.pungdong.domain.schedule;

import com.diving.pungdong.domain.Location;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    private LocalTime startTime;

    private LocalTime endTime;

    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;
}
