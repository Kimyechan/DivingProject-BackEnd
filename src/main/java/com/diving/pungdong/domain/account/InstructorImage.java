package com.diving.pungdong.domain.account;

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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InstructorImgCategory category;

    private String fileURL;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account instructor;
}
