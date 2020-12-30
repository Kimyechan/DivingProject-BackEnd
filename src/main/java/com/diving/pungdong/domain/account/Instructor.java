package com.diving.pungdong.domain.account;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Instructor extends Account{
    @OneToMany(mappedBy = "instructor")
    private List<InstructorStudent> students = new ArrayList<>();
    private Long income;
}
