package com.diving.pungdong.domain.schedule;

import com.diving.pungdong.domain.lecture.Lecture;
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
public class Schedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer period;

    private LocalTime startTime;

    private Integer currentNumber;

    private Integer maxNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<LocalDate> dates;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @PrePersist
    public void prePersist() {
        this.currentNumber = this.currentNumber == null ? 0 : this.currentNumber;
    }
}
