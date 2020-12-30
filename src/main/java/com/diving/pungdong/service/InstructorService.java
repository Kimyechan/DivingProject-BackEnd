package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.repo.InstructorJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorJpaRepo instructorJpaRepo;

    public Instructor saveInstructor(Instructor instructor) {
        return instructorJpaRepo.save(instructor);
    }
}
