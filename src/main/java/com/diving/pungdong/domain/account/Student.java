package com.diving.pungdong.domain.account;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Student extends Account{
    /**
     * 수강생 강사 매핑
     */
    @OneToMany(mappedBy = "student")
    private List<InstructorStudent> instructors = new ArrayList<>();
}
