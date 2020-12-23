package com.diving.pungdong.domain.payment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Payment {

    @Id @GeneratedValue
    Long id;
}
