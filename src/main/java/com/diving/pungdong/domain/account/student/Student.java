package com.diving.pungdong.domain.account.student;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorStudent;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@SuperBuilder
public class Student extends Account {
    /**
     * 수강생 강사 매핑
     */
    @OneToMany(mappedBy = "student")
    private List<InstructorStudent> instructors = new ArrayList<>();
}
