package com.diving.pungdong.domain.account;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Instructor extends Account{
    /**
     * 강사 수강생 매핑
     */

    @OneToMany(mappedBy = "instructor")
    List<InstructorStudent> students = new ArrayList<>();
}
