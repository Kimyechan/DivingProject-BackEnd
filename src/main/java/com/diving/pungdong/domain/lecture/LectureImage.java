package com.diving.pungdong.domain.lecture;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class LectureImage {
    @Id @GeneratedValue
    private Long id;
    private String fileURI;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

}
