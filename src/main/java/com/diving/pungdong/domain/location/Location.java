package com.diving.pungdong.domain.location;

import com.diving.pungdong.domain.lecture.Lecture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    private Double latitude;
    private Double longitude;

    @OneToOne(mappedBy = "location", fetch = FetchType.LAZY)
    private Lecture lecture;
}
