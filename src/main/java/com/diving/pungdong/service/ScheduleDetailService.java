package com.diving.pungdong.service;

import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.domain.schedule.ScheduleTime;
import com.diving.pungdong.dto.reservation.ReservationDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleDetailService {
    private final ScheduleTimeService scheduleTimeService;

    public void plusCurrentStudentNumber(List<ReservationDateDto> reservationDateDtoList, List<ScheduleDetail> scheduleDetails) {
        for (ScheduleDetail scheduleDetail : scheduleDetails) {
            ReservationDateDto temp = null;
            for (ReservationDateDto reservationDateDto : reservationDateDtoList) {
                if (scheduleDetail.getDate().equals(reservationDateDto.getDate()))  {
                    temp = reservationDateDto;
                    break;
                }
            }

            for (ScheduleTime scheduleTime : scheduleDetail.getScheduleTimes()) {
                if (temp != null && scheduleTime.getStartTime().equals(temp.getTime())) {
                    scheduleTimeService.updatePlusCurrentNumber(scheduleTime);
                }
            }
        }
    }
}
