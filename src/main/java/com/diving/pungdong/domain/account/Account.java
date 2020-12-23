package com.diving.pungdong.domain.account;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
public class Account {

    @Id @GeneratedValue
    Long id;

    String userId;

    String password;

    String userName;

    Integer age;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @Enumerated(EnumType.STRING)
    Role role;
}
