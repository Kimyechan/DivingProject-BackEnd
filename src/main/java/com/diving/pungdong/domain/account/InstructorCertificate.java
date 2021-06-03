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
public class InstructorCertificate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileURL;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account instructor;
}
