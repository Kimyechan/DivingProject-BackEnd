package com.diving.pungdong.domain.account;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@SuperBuilder
public class Instructor extends Account{
    @OneToMany(mappedBy = "instructor")
    private List<InstructorStudent> students = new ArrayList<>();
    private Long income;
}
