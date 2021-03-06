package com.diving.pungdong.service;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("강의 비용 계산")
    public void calcCost() {
        List<String> equipmentNameList = new ArrayList<>();
        equipmentNameList.add("오리발");
        equipmentNameList.add("슈트");

        List<Equipment> equipmentList = createEquipmentList();
        Lecture lecture = Lecture.builder()
                .price(50000)
                .equipmentList(equipmentList)
                .build();

        Integer cost = paymentService.calcCost(equipmentNameList, lecture);

        assertThat(cost).isEqualTo(65000);
    }

    public List<Equipment> createEquipmentList() {
        List<Equipment> equipmentList = new ArrayList<>();

        Equipment equipment1 = Equipment.builder()
                .name("오리발")
                .price(5000)
                .build();
        equipmentList.add(equipment1);

        Equipment equipment2 = Equipment.builder()
                .name("슈트")
                .price(10000)
                .build();
        equipmentList.add(equipment2);

        return equipmentList;
    }
}