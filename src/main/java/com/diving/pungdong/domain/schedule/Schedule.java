package com.diving.pungdong.domain.schedule;

import com.diving.pungdong.domain.lecture.Lecture;
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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer currentNumber;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ScheduleDateTime> scheduleDateTimes;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @PrePersist
    public void prePersist() {
        this.currentNumber = this.currentNumber == null ? 0 : this.currentNumber;
    }
}
