package com.diving.pungdong.service;

import com.diving.pungdong.controller.lecture.LectureController.LectureUpdateInfo;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.repo.SwimmingPoolJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SwimmingPoolService {

    private final SwimmingPoolJpaRepo swimmingPoolJpaRepo;

    public SwimmingPool getSwimmingPool(Long swimmingPoolId){
        return swimmingPoolJpaRepo.findById(swimmingPoolId).orElse(new SwimmingPool());
    }

    public SwimmingPool saveSwimmingPool(SwimmingPool swimmingPool) {
        return swimmingPoolJpaRepo.save(swimmingPool);
    }

    public SwimmingPool changeSwimmingPool(LectureUpdateInfo lectureUpdateInfo) {
        Location location = lectureUpdateInfo.getSwimmingPoolLocation();
        SwimmingPool swimmingPool = swimmingPoolJpaRepo.findByLocation(location).orElse(SwimmingPool.builder().location(location).build());
        return swimmingPoolJpaRepo.save(swimmingPool);
    }
}
