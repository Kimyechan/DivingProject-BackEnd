package com.diving.pungdong.domain.account;

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
