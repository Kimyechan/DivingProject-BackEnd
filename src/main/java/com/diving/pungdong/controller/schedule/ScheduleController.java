package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateResult;
import com.diving.pungdong.dto.schedule.read.ScheduleInfo;
import com.diving.pungdong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/schedule", produces = MediaTypes.HAL_JSON_VALUE)
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<?> createSchedule(@CurrentUser Account account,
                                            @Valid @RequestBody ScheduleCreateInfo scheduleCreateInfo,
                                            BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        Schedule schedule = scheduleService.saveScheduleInfo(account, scheduleCreateInfo);
        ScheduleCreateResult scheduleCreateResult = new ScheduleCreateResult(schedule.getId());

        EntityModel<ScheduleCreateResult> model = EntityModel.of(scheduleCreateResult);
        WebMvcLinkBuilder linkBuilder = linkTo(methodOn(ScheduleController.class).createSchedule(account, scheduleCreateInfo, result));
        model.add(linkBuilder.withSelfRel());

        return ResponseEntity.created(linkBuilder.toUri()).body(model);
    }

    @GetMapping
    public ResponseEntity<?> findScheduleByMonth(@NotNull @RequestParam Long lectureId,
                                                 @NotNull @RequestParam int month) {
        List<Schedule> schedules = scheduleService.findLectureScheduleByMonth(lectureId, Month.of(month), LocalDate.now());
        List<ScheduleInfo> scheduleInfos = scheduleService.mapToScheduleInfos(schedules);

        CollectionModel<ScheduleInfo> model = CollectionModel.of(scheduleInfos);
        model.add(linkTo(methodOn(ScheduleController.class).findScheduleByMonth(lectureId, month)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-schedule-read-list").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }
}
