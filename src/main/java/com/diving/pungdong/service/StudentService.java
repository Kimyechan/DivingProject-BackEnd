package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Student;
import com.diving.pungdong.repo.StudentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentJpaRepo studentJpaRepo;

    public Student saveStudent(Student student) {
        return studentJpaRepo.save(student);
    }
}
