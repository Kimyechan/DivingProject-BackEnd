package com.diving.pungdong.domain.account;

import com.diving.pungdong.domain.account.instructor.Instructor;
import com.diving.pungdong.domain.account.student.Student;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class InstructorStudent {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Instructor instructor;

    @ManyToOne
    private Student student;
}
