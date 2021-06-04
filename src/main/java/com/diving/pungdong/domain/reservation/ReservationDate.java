package com.diving.pungdong.domain.reservation;

import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private LocalTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleDetail scheduleDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleTime scheduleTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;
}
