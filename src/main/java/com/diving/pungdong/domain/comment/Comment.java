package com.diving.pungdong.domain.comment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Comment {

    @Id @GeneratedValue
    private Long id;
}
