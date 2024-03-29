package com.diving.pungdong.domain.lecture;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.schedule.Schedule;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String classKind;

    @Enumerated(EnumType.STRING)
    private Organization organization;

    private String level;

    private String description;

    private Integer price;

    private String region;

    @ElementCollection(fetch = FetchType.LAZY)
    private Set<String> serviceTags = new HashSet<>();

    private LocalDateTime registrationDate;

    private Integer maxNumber;

    private Integer period;

    private LocalTime lectureTime;

    private Float reviewTotalAvg;

    private Integer reviewCount;

    private Boolean isClosed;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LectureImage> lectureImages;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Equipment> equipmentList;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account instructor;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LectureMark> lectureMarks;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToOne(fetch = FetchType.LAZY)
    private Location location;

    @PrePersist
    public void prePersist() {
        this.registrationDate = this.registrationDate == null ? LocalDateTime.now() : this.registrationDate;
        this.reviewTotalAvg = this.reviewTotalAvg == null ? 0 : this.reviewTotalAvg;
        this.reviewCount = this.reviewCount == null ? 0 : this.reviewCount;
        this.isClosed = false;
    }
}
