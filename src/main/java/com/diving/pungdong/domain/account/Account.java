package com.diving.pungdong.domain.account;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.lecture.Organization;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    private String email;

    private String password;

    private String nickName;

    private String birth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(fetch = FetchType.LAZY)
    private ProfilePhoto profilePhoto;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<Role> roles = new HashSet<>();

    private String phoneNumber;

    private Organization organization;

    @Lob
    private String selfIntroduction;

    private Boolean isRequestCertified;

    private Boolean isCertified;

    private Long income;

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<InstructorCertificate> instructorCertificates;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<LectureMark> lectureMarks;

    @PrePersist
    public void prePersist() {
        this.income = this.income == null ? 0 : this.income;
        this.isRequestCertified = this.isRequestCertified != null && this.isRequestCertified;
        this.isCertified = this.isCertified != null && this.isCertified;
    }
}
