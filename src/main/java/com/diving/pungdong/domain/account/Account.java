package com.diving.pungdong.domain.account;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.ArrayList;
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

    String userName;

    Integer age;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private List<InstructorStudent> instructorList;

    private String phoneNumber;

    private String groupName;

    private String description;

    @Column(columnDefinition = "integer default 0")
    private Long income;

    @OneToMany(mappedBy = "instructor")
    private List<InstructorImage> instructorImages = new ArrayList<>();

    @OneToMany(mappedBy = "instructor")
    private List<InstructorStudent> studentList = new ArrayList<>();
}
