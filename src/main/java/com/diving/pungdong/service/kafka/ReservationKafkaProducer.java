package com.diving.pungdong.service.kafka;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.service.kafka.dto.reservation.ReservationCreateInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationKafkaProducer {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void sendEnrollReservationEvent(Account student, Account instructor, Lecture lecture, Schedule schedule) {
        String messageBody = student.getNickName() + "이 " + lecture.getTitle() + " 강의를 예약하셨습니다";

        ReservationCreateInfo reservationCreateInfo = ReservationCreateInfo.builder()
                .instructorId(String.valueOf(instructor.getId()))
                .lectureId(String.valueOf(lecture.getId()))
                .scheduleId(String.valueOf(schedule.getId()))
                .messageBody(messageBody)
                .build();

        kafkaTemplate.send("create-reservation", reservationCreateInfo);
    }
}
