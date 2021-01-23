package com.diving.pungdong.service;

import com.diving.pungdong.controller.lecture.LectureController.EquipmentUpdate;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import org.hibernate.service.spi.InjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@Transactional
class EquipmentServiceTest {
    private EquipmentService equipmentService;

    @Mock
    private EquipmentJpaRepo equipmentJpaRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        equipmentService = new EquipmentService(equipmentJpaRepo);
    }

    @Test
    @DisplayName("강의 대여 장비 리스트 수정")
    public void updateList() {
        EquipmentUpdate equipmentUpdate1 = EquipmentUpdate.builder()
                .name("장비 1")
                .price(10000)
                .isDeleted(false)
                .build();
        EquipmentUpdate equipmentUpdate2 = EquipmentUpdate.builder()
                .name("장비 2")
                .price(10000)
                .isDeleted(true)
                .build();
        EquipmentUpdate equipmentUpdate3 = EquipmentUpdate.builder()
                .name("장비 3")
                .price(10000)
                .isDeleted(false)
                .build();

        List<EquipmentUpdate> equipmentUpdateList = new ArrayList<>();
        equipmentUpdateList.add(equipmentUpdate1);
        equipmentUpdateList.add(equipmentUpdate2);
        equipmentUpdateList.add(equipmentUpdate3);

        Lecture lecture = Lecture.builder().build();

        Equipment equipment = Equipment.builder()
                .build();

        given(equipmentJpaRepo.findByName(equipmentUpdate1.getName())).willReturn(Optional.empty());
        given(equipmentJpaRepo.findByName(equipmentUpdate2.getName())).willReturn(Optional.ofNullable(equipment));
        given(equipmentJpaRepo.findByName(equipmentUpdate3.getName())).willReturn(Optional.ofNullable(equipment));
        equipmentService.lectureEquipmentUpdate(equipmentUpdateList, lecture);

        verify(equipmentJpaRepo, times(1)).deleteByName(equipmentUpdate2.getName());
        verify(equipmentJpaRepo, times(1)).save(any());
    }
}