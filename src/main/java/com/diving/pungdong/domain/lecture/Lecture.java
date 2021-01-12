package com.diving.pungdong.domain.lecture;

import com.diving.pungdong.domain.account.instructor.Instructor;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"instructor", "swimmingPool", "lectureImages"})
public class Lecture {

    @Id @GeneratedValue
    private Long id;

    private String title;

    private String classKind;

    private String groupName;

    private String certificateKind;

    private String description;

    private Integer price;

    private Integer period;

    private Integer studentCount;

    private String region;

    @Builder.Default
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LectureImage> lectureImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    private SwimmingPool swimmingPool;
}
