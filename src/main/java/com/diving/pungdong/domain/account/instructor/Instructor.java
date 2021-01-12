package com.diving.pungdong.domain.account.instructor;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorStudent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@SuperBuilder
public class Instructor extends Account {
    private String phoneNumber;

    private String groupName;

    private String description;

    @Column(columnDefinition = "integer default 0")
    private Long income;

    @OneToMany(mappedBy = "instructor")
    private List<InstructorImage> instructorImages = new ArrayList<>();

    @OneToMany(mappedBy = "instructor")
    private List<InstructorStudent> students = new ArrayList<>();
}
