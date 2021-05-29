package com.diving.pungdong.domain.account;

import com.diving.pungdong.domain.LectureMark;
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
    @Id @GeneratedValue
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

    private String groupName;

    @Lob
    private String description;

    @Column(columnDefinition = "integer default 0")
    private Long income;

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<InstructorImage> instructorImages;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<LectureMark> lectureMarks;
}
