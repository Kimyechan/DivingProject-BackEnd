package com.diving.pungdong.service;

import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.payment.Payment;
import com.diving.pungdong.repo.PaymentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentJpaRepo paymentJpaRepo;

    public Integer calcCost(List<String> equipmentNameList, Lecture lecture) {
        Integer cost = lecture.getPrice();
        for (Equipment equipment : lecture.getEquipmentList()) {
            for (String equipmentName : equipmentNameList) {
                if (equipmentName.equals(equipment.getName())) {
                    cost += equipment.getPrice();
                }
            }
        }

        return cost;
    }

    public Payment savePayment(Integer cost) {
        Payment payment = Payment.builder()
                .cost(cost)
                .build();

        return paymentJpaRepo.save(payment);
    }
}
