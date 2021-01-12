package com.diving.pungdong.domain.account;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@Getter @Setter @EqualsAndHashCode(of = "id")
@SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id @GeneratedValue
    Long id;

    @Email
    @Column(nullable = false, unique = true)
    String email;

    String password;

    String userName;

    Integer age;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<Role> roles;
}
