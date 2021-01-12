package com.diving.pungdong.domain.account.instructor;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class InstructorImage {

    @Id @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private InstructorImgCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Instructor instructor;
}
