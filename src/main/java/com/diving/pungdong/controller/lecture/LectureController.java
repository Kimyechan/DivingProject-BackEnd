package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.SwimmingPoolService;
import lombok.*;
import lombok.experimental.WithBy;
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

import javax.validation.constraints.NotEmpty;
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
    private final SwimmingPoolService swimmingPoolService;
    private final AccountService accountService;
    private final S3Uploader s3Uploader;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity createLecture(Authentication authentication,
                                        @RequestPart("request") CreateLectureReq createLectureReq,
                                        @RequestPart("fileList") List<MultipartFile> fileList) throws IOException {
        Account instructor = accountService.findAccountByEmail(authentication.getName());

        // ToDo: 수영장 없애고 위치정보 따로 (위도 경도)
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class CreateLectureReq {
        @NotEmpty private String title;
        @NotEmpty private String classKind;
        @NotEmpty private String groupName;
        @NotEmpty private String certificateKind;
        @NotEmpty private String description;
        @NotEmpty private Integer price;
        @NotEmpty private Integer period;
        @NotEmpty private Integer studentCount;
        @NotEmpty private String region;
        private List<EquipmentDto> equipmentList = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentDto {
        private String name;
        private Integer price;
    }

    @Data
    @AllArgsConstructor
    static class CreateLectureRes {
        private Long lectureId;
        private String title;
        private String instructorName;
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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LectureUpdateInfo {
        private Long id;
        private String title;
        private String classKind;
        private String groupName;
        private String certificateKind;
        private String description;
        private Integer price;
        private Integer period;
        private Integer studentCount;
        private String region;
        private List<LectureImageUpdate> lectureImageUpdateList;
        private List<EquipmentUpdate> equipmentUpdateList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LectureImageUpdate {
        String lectureImageURL;
        Boolean isDeleted;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EquipmentUpdate {
        private String name;
        private Integer price;
        Boolean isDeleted;
    }

    @Data
    @Builder
    @AllArgsConstructor
    static class LectureUpdateRes {
        private Long id;
        private String title;
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

    @Data
    @Builder
    static class LectureDeleteRes {
        private Long lectureId;
    }

    @GetMapping("/detail")
    public ResponseEntity<EntityModel<LectureDetail>> getLectureDetail(@RequestParam Long id) {
        Lecture lecture = lectureService.getLectureById(id);
        // ToDo: 일정 보기 추가 - 위치 정보 강의 일정이랑 묶기, 시간도 같이 묶기
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

    @Data
    @Builder
    @AllArgsConstructor
    static class LectureDetail {
        private Long id;
        private String title;
        private String classKind;
        private String groupName;
        private String certificateKind;
        private String description;
        private Integer price;
        private Integer period;
        private Integer studentCount;
        private String region;
        private Long instructorId;
        private List<String> lectureUrlList;
        private List<EquipmentDto> equipmentList;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class LectureByRegionReq {
        @NotEmpty private String region;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class LectureByRegionRes {
        private Long id;
        private String title;
        private String classKind;
        private String groupName;
        private String certificateKind;
        private Integer price;
        private String region;
        private List<String> imageURL = new ArrayList<>();
    }

    @PostMapping("/upload")
    public String upload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return s3Uploader.upload(file, "lecture", authentication.getName());
    }
}
