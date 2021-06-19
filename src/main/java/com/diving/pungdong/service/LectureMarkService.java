package com.diving.pungdong.service;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.repo.LectureMarkJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureMarkService {
    private final LectureMarkJpaRepo lectureMarkJpaRepo;

    @Transactional(readOnly = true)
    public Page<LectureMark> findLectureMarksByAccount(Account account, Pageable pageable) {
        return lectureMarkJpaRepo.findByAccount(account, pageable);
    }
}
