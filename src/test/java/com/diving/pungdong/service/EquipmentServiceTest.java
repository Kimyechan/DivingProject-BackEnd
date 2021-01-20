package com.diving.pungdong.service;

import com.diving.pungdong.repo.EquipmentJpaRepo;
import org.hibernate.service.spi.InjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Transactional
class EquipmentServiceTest {
    @InjectMocks
    private EquipmentService equipmentService;

    @Mock
    private EquipmentJpaRepo equipmentJpaRepo;

    @Test
    @DisplayName("강의 대여 장비 리스트 수정")
    public void updateList() {

    }


}