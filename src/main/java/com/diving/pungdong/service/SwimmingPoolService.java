package com.diving.pungdong.service;

import com.diving.pungdong.controller.swimmingpool.SwimmingPoolController;
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
}
