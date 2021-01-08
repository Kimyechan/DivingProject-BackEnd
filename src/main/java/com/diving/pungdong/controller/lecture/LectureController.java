package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.service.InstructorService;
import com.diving.pungdong.service.LectureImageService;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.SwimmingPoolService;
import lombok.*;
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
    private final LectureImageService lectureImageService;
    private final SwimmingPoolService swimmingPoolService;
    private final InstructorService instructorService;
    private final S3Uploader s3Uploader;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity createLecture(Authentication authentication,
                                        @RequestPart("request") CreateLectureReq createLectureReq,
                                        @RequestPart("file") MultipartFile file) throws IOException {
        Instructor instructor = instructorService.getInstructorByEmail(authentication.getName());
        SwimmingPool swimmingPool = swimmingPoolService.getSwimmingPool(createLectureReq.getSwimmingPoolId());

        Lecture lecture = Lecture.builder()
                .title(createLectureReq.getTitle())
                .description(createLectureReq.getDescription())
                .kind(createLectureReq.getKind())
                .period(createLectureReq.getPeriod())
                .price(createLectureReq.getPrice())
                .studentCount(createLectureReq.getStudentCount())
                .region(createLectureReq.getRegion())
                .instructor(instructor)
                .swimmingPool(swimmingPool)
                .build();

        Lecture savedLecture = lectureService.saveLecture(lecture);

        String fileURI = s3Uploader.upload(file, "lecture", authentication.getName());
        LectureImage lectureImage = LectureImage.builder()
                .fileURI(fileURI)
                .lecture(savedLecture)
                .build();

        lectureImageService.saveLectureImage(lectureImage);

        CreateLectureRes createLectureRes
                = new CreateLectureRes(lecture.getTitle(), lecture.getInstructor().getUserName(), fileURI);

        EntityModel<CreateLectureRes> model = EntityModel.of(createLectureRes);
        WebMvcLinkBuilder selfLink = linkTo(methodOn(LectureController.class).createLecture(authentication, createLectureReq, file));
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
        @NotEmpty private String kind;
        @NotEmpty private String description;
        @NotEmpty private Integer price;
        @NotEmpty private Integer period;
        @NotEmpty private Integer studentCount;
        @NotEmpty private String region;
        @NotEmpty private Long swimmingPoolId;
    }

    @Data
    @AllArgsConstructor
    static class CreateLectureRes {
        private String title;
        private String instructorName;
        private String fileURI;
    }

    @PostMapping("/upload")
    public String upload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return s3Uploader.upload(file, "lecture", authentication.getName());
    }

    @GetMapping("/list/region")
    public ResponseEntity<PagedModel<EntityModel<LectureByRegionRes>>> getListByRegion(LectureByRegionReq lectureByRegionReq,
                                                                                       Pageable pageable,
                                                                                       PagedResourcesAssembler<LectureByRegionRes> assembler
                                                                            ) {
        Page<Lecture> lectures = lectureService.getListByRegion(lectureByRegionReq.getRegion(), pageable);
        List<LectureByRegionRes> lectureByRegionRes = new ArrayList<>();
        for (Lecture lecture : lectures) {
            List<String> imageURLs = new ArrayList<>();
            for (LectureImage image : lecture.getLectureImage()) {
                imageURLs.add(image.getFileURI());
            }
            LectureByRegionRes res = LectureByRegionRes.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .kind(lecture.getKind())
                    .price(lecture.getPrice())
                    .region(lecture.getRegion())
                    .imageURL(imageURLs)
                    .build();
            lectureByRegionRes.add(res);
        }

        Page<LectureByRegionRes> result = new PageImpl<>(lectureByRegionRes, pageable, lectureByRegionRes.size());
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
        private String kind;
        private Integer price;
        private String region;
        private List<String> imageURL = new ArrayList<>();
    }

}
