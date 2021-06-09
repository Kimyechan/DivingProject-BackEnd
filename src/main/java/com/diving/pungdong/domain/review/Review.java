package com.diving.pungdong.domain.review;

import com.diving.pungdong.domain.account.Account;
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
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float instructorStar;

    private Float lectureStar;

    private Float locationStar;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account writer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ReviewImage> reviewImages;
}
