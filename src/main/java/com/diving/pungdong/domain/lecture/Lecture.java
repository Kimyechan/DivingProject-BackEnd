package com.diving.pungdong.domain.lecture;

import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"instructor", "swimmingPool"})
public class Lecture {

    @Id @GeneratedValue
    private Long id;

    private String title;

    private String kind;

    private String description;

    private Integer price;

    private Integer period;

    private Integer studentCount;

    private String region;

    @Builder.Default
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LectureImage> lectureImage = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    private SwimmingPool swimmingPool;
}
