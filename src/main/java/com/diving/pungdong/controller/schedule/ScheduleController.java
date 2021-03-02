package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.model.schedule.ScheduleCreateReq;
import com.diving.pungdong.model.schedule.ScheduleCreateRes;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
}
