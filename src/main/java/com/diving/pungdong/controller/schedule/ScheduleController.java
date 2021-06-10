package com.diving.pungdong.controller.schedule;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateInfo;
import com.diving.pungdong.dto.schedule.create.ScheduleCreateResult;
import com.diving.pungdong.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

//    @GetMapping
//    public ResponseEntity<?> read(@RequestParam Long lectureId) {
//        List<Schedule> newScheduleList = scheduleService.filterListByCheckingPast(lectureId);
//
//        List<ScheduleDto> scheduleDtoList = mapToScheduleDtoList(newScheduleList);
//
//        CollectionModel<ScheduleDto> model = CollectionModel.of(scheduleDtoList);
//        model.add(linkTo(methodOn(ScheduleController.class).read(lectureId)).withSelfRel());
//        return ResponseEntity.ok().body(model);
//    }
//
//
//    public List<ScheduleDto> mapToScheduleDtoList(List<Schedule> scheduleList) {
//        List<ScheduleDto> schedules = new ArrayList<>();
//
//        for (Schedule schedule : scheduleList) {
//            List<ScheduleDetailDto> scheduleDetails = mapToScheduleDetailDtoList(schedule);
//
//            ScheduleDto dto = ScheduleDto.builder()
//                    .scheduleId(schedule.getId())
//                    .period(schedule.getPeriod())
//                    .maxNumber(schedule.getMaxNumber())
//                    .scheduleDetails(scheduleDetails)
//                    .build();
//            schedules.add(dto);
//        }
//
//        return schedules;
//    }
//
//    public List<ScheduleDetailDto> mapToScheduleDetailDtoList(Schedule schedule) {
//        List<ScheduleDetailDto> scheduleDetails = new ArrayList<>();
//
//        for (ScheduleDate scheduleDate : schedule.getScheduleDates()) {
//            List<ScheduleTimeDto> scheduleTimeDtoList = mapToScheduleTimeDtoList(scheduleDate.getScheduleTimes());
//
//            ScheduleDetailDto detailDto = ScheduleDetailDto.builder()
//                    .scheduleDetailId(scheduleDate.getId())
//                    .date(scheduleDate.getDate())
//                    .scheduleTimeDtoList(scheduleTimeDtoList)
//                    .lectureTime(scheduleDate.getLectureTime())
//                    .location(scheduleDate.getLocation())
//                    .build();
//            scheduleDetails.add(detailDto);
//        }
//
//        return scheduleDetails;
//    }
//
//    private List<ScheduleTimeDto> mapToScheduleTimeDtoList(List<ScheduleTime> scheduleTimes) {
//        List<ScheduleTimeDto> scheduleTimeDtoList = new ArrayList<>();
//
//        for (ScheduleTime scheduleTime : scheduleTimes) {
//            ScheduleTimeDto scheduleTimeDto = ScheduleTimeDto.builder()
//                    .scheduleTimeId(scheduleTime.getId())
//                    .startTime(scheduleTime.getStartTime())
//                    .currentNumber(scheduleTime.getCurrentNumber())
//                    .build();
//            scheduleTimeDtoList.add(scheduleTimeDto);
//        }
//
//        return scheduleTimeDtoList;
//    }
}
