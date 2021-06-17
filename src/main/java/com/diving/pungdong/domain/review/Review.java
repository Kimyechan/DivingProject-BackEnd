package com.diving.pungdong.domain.review;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
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

    private Float totalStarAvg;

    private String description;

    private LocalDate writeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account writer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ReviewImage> reviewImages;

    @PrePersist
    public void prePersist() {
        this.totalStarAvg = (this.instructorStar + this.lectureStar + this.locationStar) / 3;
        this.writeDate = this.writeDate == null ? LocalDate.now() : this.writeDate;
    }
}
