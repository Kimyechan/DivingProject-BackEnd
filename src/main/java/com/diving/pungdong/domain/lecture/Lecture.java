package com.diving.pungdong.domain.lecture;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.domain.review.Review;
import com.diving.pungdong.domain.schedule.Schedule;
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
@ToString(exclude = {"instructor", "equipmentList", "lectureImages"})
public class Lecture {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String classKind;

    @Enumerated(EnumType.STRING)
    private Organization organization;

    private String level;

    private String description;

    private Integer price;

    private String region;

    private LocalDate registrationDate;

    private Integer maxNumber;

    private LocalTime lectureTime;

    private Float reviewTotalAvg;

    private Integer reviewCount;

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

    @OneToOne(mappedBy = "lecture", fetch = FetchType.LAZY)
    private Location location;
}
