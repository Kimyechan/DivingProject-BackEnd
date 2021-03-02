package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.schedule.read.ScheduleDetailDto;
import com.diving.pungdong.dto.schedule.read.ScheduleDto;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateReq;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateRes;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/schedule", produces = MediaTypes.HAL_JSON_VALUE)
public class ScheduleController {

    private final LectureService lectureService;
    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ScheduleCreateReq scheduleCreateReq) {
        Lecture lecture = lectureService.getLectureById(scheduleCreateReq.getLectureId());
        Schedule savedSchedule = scheduleService.saveScheduleTx(lecture, scheduleCreateReq);

        ScheduleCreateRes scheduleCreateRes = ScheduleCreateRes.builder()
                .lectureId(lecture.getId())
                .scheduleId(savedSchedule.getId())
                .build();

        EntityModel<ScheduleCreateRes> model = EntityModel.of(scheduleCreateRes);
        WebMvcLinkBuilder linkBuilder = linkTo(methodOn(ScheduleController.class).create(scheduleCreateReq));
        URI uri = linkBuilder.toUri();
        model.add(linkBuilder.withSelfRel());

        return ResponseEntity.created(uri).body(model);
    }

    @GetMapping
    public ResponseEntity<?> read(@RequestParam Long lectureId) {
        List<Schedule> scheduleList = scheduleService.getByLectureId(lectureId);

        List<ScheduleDto> scheduleDtoList = mapToScheduleDtoList(scheduleList);
        return ResponseEntity.ok().build();
    }

    public List<ScheduleDto> mapToScheduleDtoList(List<Schedule> scheduleList) {
        List<ScheduleDto> schedules = new ArrayList<>();

        for (Schedule schedule : scheduleList) {
            List<ScheduleDetailDto> scheduleDetails = mapToScheduleDetailDtoList(schedule);
            ScheduleDto dto = ScheduleDto.builder()
                    .period(schedule.getPeriod())
                    .scheduleDetails(scheduleDetails)
                    .build();
            schedules.add(dto);
        }

        return schedules;
    }

    public List<ScheduleDetailDto> mapToScheduleDetailDtoList(Schedule schedule) {
        List<ScheduleDetailDto> scheduleDetails = new ArrayList<>();

        for (ScheduleDetail scheduleDetail : schedule.getScheduleDetails()) {
            ScheduleDetailDto detailDto = ScheduleDetailDto.builder()
                    .date(scheduleDetail.getDate())
                    .startTimes(scheduleDetail.getStartTimes())
                    .lectureTime(scheduleDetail.getLectureTime())
                    .location(scheduleDetail.getLocation())
                    .build();
            scheduleDetails.add(detailDto);
        }

        return scheduleDetails;
    }
}
