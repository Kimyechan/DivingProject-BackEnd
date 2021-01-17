package com.diving.pungdong.domain.equipment;

import com.diving.pungdong.domain.lecture.Lecture;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Equipment {
    @Id @GeneratedValue
    private Long id;

    private String name;

    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;
}
