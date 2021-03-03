package com.diving.pungdong.domain.lecture;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureImage {
    @Id @GeneratedValue
    private Long id;
    private String fileURI;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;


}
