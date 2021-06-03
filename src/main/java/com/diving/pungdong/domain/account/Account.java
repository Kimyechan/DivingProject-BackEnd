package com.diving.pungdong.domain.account;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.lecture.Organization;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Email
    String email;

    String password;

    String nickName;

    String birth;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<Role> roles = new HashSet<>();

    private String phoneNumber;

    private Boolean isCertified;

    private Organization organization;

    @Lob
    private String selfIntroduction;

    @Column(columnDefinition = "integer default 0")
    private Long income;

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<InstructorImage> instructorImages;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<LectureMark> lectureMarks;
}
