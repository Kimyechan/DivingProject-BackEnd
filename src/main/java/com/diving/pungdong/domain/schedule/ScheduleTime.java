package com.diving.pungdong.domain.schedule;

import com.diving.pungdong.domain.reservation.ReservationDate;
import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;

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

    @OneToMany(mappedBy = "scheduleTime", fetch = FetchType.LAZY)
    private List<ReservationDate> reservationDates;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleDetail scheduleDetail;
}
