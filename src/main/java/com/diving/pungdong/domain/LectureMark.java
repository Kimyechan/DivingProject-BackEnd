package com.diving.pungdong.domain;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureMark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;
}
