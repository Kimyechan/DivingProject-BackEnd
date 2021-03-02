package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.lecture.create.CreateLectureReq;
import com.diving.pungdong.dto.lecture.create.CreateLectureRes;
import com.diving.pungdong.dto.lecture.create.EquipmentDto;
import com.diving.pungdong.dto.lecture.delete.LectureDeleteRes;
import com.diving.pungdong.dto.lecture.detail.LectureDetail;
import com.diving.pungdong.dto.lecture.detail.ScheduleDetailDto;
import com.diving.pungdong.dto.lecture.detail.ScheduleDto;
import com.diving.pungdong.dto.lecture.search.LectureByRegionReq;
import com.diving.pungdong.dto.lecture.search.LectureByRegionRes;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.dto.lecture.update.LectureUpdateRes;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/lecture", produces = MediaTypes.HAL_JSON_VALUE)
public class LectureController {

    private final LectureService lectureService;
    private final AccountService accountService;
    private final S3Uploader s3Uploader;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity createLecture(Authentication authentication,
                                        @RequestPart("request") CreateLectureReq createLectureReq,
                                        @RequestPart("fileList") List<MultipartFile> fileList) throws IOException {
        Account instructor = accountService.findAccountByEmail(authentication.getName());

        Lecture lecture = Lecture.builder()
                .title(createLectureReq.getTitle())
                .description(createLectureReq.getDescription())
                .classKind(createLectureReq.getClassKind())
                .groupName(createLectureReq.getGroupName())
                .certificateKind(createLectureReq.getCertificateKind())
                .period(createLectureReq.getPeriod())
                .price(createLectureReq.getPrice())
                .studentCount(createLectureReq.getStudentCount())
                .region(createLectureReq.getRegion())
                .instructor(instructor)
                .build();

        String email = authentication.getName();

        List<Equipment> equipmentList = new ArrayList<>();
        for (EquipmentDto equipmentDto : createLectureReq.getEquipmentList()) {
            Equipment equipment = Equipment.builder()
                    .name(equipmentDto.getName())
                    .price(equipmentDto.getPrice())
                    .build();
            equipmentList.add(equipment);
        }

        Lecture savedLecture = lectureService.createLecture(email, fileList, lecture, equipmentList);

        CreateLectureRes createLectureRes
                = new CreateLectureRes(savedLecture.getId(), savedLecture.getTitle(), savedLecture.getInstructor().getUserName());

        EntityModel<CreateLectureRes> model = EntityModel.of(createLectureRes);
        WebMvcLinkBuilder selfLink = linkTo(methodOn(LectureController.class).createLecture(authentication, createLectureReq, fileList));
        model.add(selfLink.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-create").withRel("profile"));

        return ResponseEntity.created(selfLink.toUri()).body(model);
    }

    @PostMapping("/update")
    public ResponseEntity<EntityModel<LectureUpdateRes>> updateLecture(Authentication authentication,
                                                                       @RequestPart("request") LectureUpdateInfo lectureUpdateInfo,
                                                                       @RequestPart("fileList") List<MultipartFile> addLectureImageFiles) throws IOException {
        Lecture lecture = lectureService.getLectureById(lectureUpdateInfo.getId());
        String email = authentication.getName();

        if (!lecture.getInstructor().getEmail().equals(email)) {
            throw new NoPermissionsException();
        }

        Lecture updatedLecture = lectureService.updateLectureTx(email, lectureUpdateInfo, addLectureImageFiles, lecture);
        LectureUpdateRes lectureUpdateRes = LectureUpdateRes.builder()
                .id(updatedLecture.getId())
                .title(updatedLecture.getTitle())
                .build();

        EntityModel<LectureUpdateRes> model = EntityModel.of(lectureUpdateRes);
        model.add(linkTo(methodOn(LectureController.class).updateLecture(authentication, lectureUpdateInfo, addLectureImageFiles)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<EntityModel<LectureDeleteRes>> deleteLecture(Authentication authentication, @RequestParam("id") Long id) {
        Lecture lecture = lectureService.getLectureById(id);
        String email = authentication.getName();
        if (!lecture.getInstructor().getEmail().equals(email)) {
            throw new NoPermissionsException();
        }

        lectureService.deleteLectureById(id);

        LectureDeleteRes lectureDeleteRes = LectureDeleteRes.builder()
                .lectureId(id)
                .build();

        EntityModel<LectureDeleteRes> model = EntityModel.of(lectureDeleteRes);
        model.add(linkTo(methodOn(LectureController.class).deleteLecture(authentication, id)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/detail")
    public ResponseEntity<EntityModel<LectureDetail>> getLectureDetail(@RequestParam Long id) {
        Lecture lecture = lectureService.getLectureById(id);

        List<ScheduleDto> schedules = getScheduleDtos(lecture);

        LectureDetail lectureDetail = LectureDetail.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .classKind(lecture.getClassKind())
                .groupName(lecture.getGroupName())
                .certificateKind(lecture.getCertificateKind())
                .description(lecture.getDescription())
                .price(lecture.getPrice())
                .period(lecture.getPeriod())
                .studentCount(lecture.getStudentCount())
                .region(lecture.getRegion())
                .instructorId(lecture.getInstructor().getId())
                .lectureUrlList(new ArrayList<>())
                .equipmentList(new ArrayList<>())
                .schedules(schedules)
                .build();

        for (LectureImage lectureImage : lecture.getLectureImages()) {
            lectureDetail.getLectureUrlList().add(lectureImage.getFileURI());
        }

        for (Equipment equipment : lecture.getEquipmentList()) {
            EquipmentDto equipmentDto = EquipmentDto.builder()
                    .name(equipment.getName())
                    .price(equipment.getPrice())
                    .build();

            lectureDetail.getEquipmentList().add(equipmentDto);
        }

        EntityModel<LectureDetail> model = EntityModel.of(lectureDetail);
        model.add(linkTo(methodOn(LectureController.class).getLectureDetail(id)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    public List<ScheduleDto> getScheduleDtos(Lecture lecture) {
        List<ScheduleDto> schedules = new ArrayList<>();
        for (Schedule schedule : lecture.getSchedules()) {
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
            ScheduleDto dto = ScheduleDto.builder()
                    .period(schedule.getPeriod())
                    .scheduleDetails(scheduleDetails)
                    .build();
            schedules.add(dto);
        }
        return schedules;
    }

    // ToDo: 겅색 필터 추가
    @GetMapping("/list/region")
    public ResponseEntity<PagedModel<EntityModel<LectureByRegionRes>>> getListByRegion(LectureByRegionReq lectureByRegionReq,
                                                                                       Pageable pageable,
                                                                                       PagedResourcesAssembler<LectureByRegionRes> assembler
                                                                            ) {
        Page<Lecture> lectures = lectureService.getListByRegion(lectureByRegionReq.getRegion(), pageable);
        List<LectureByRegionRes> lectureByRegionRes = new ArrayList<>();
        for (Lecture lecture : lectures.getContent()) {
            List<String> imageURLs = new ArrayList<>();
            for (LectureImage image : lecture.getLectureImages()) {
                imageURLs.add(image.getFileURI());
            }
            LectureByRegionRes res = LectureByRegionRes.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .classKind(lecture.getClassKind())
                    .groupName(lecture.getGroupName())
                    .certificateKind(lecture.getCertificateKind())
                    .price(lecture.getPrice())
                    .region(lecture.getRegion())
                    .imageURL(imageURLs)
                    .build();
            lectureByRegionRes.add(res);
        }

        Page<LectureByRegionRes> result = new PageImpl<>(lectureByRegionRes, pageable, lectures.getTotalElements());
        PagedModel<EntityModel<LectureByRegionRes>> model = assembler.toModel(result);
        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/upload")
    public String upload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return s3Uploader.upload(file, "lecture", authentication.getName());
    }
}
