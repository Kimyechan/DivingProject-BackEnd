package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.delete.LectureDeleteRes;
import com.diving.pungdong.dto.lecture.detail.LectureDetail;
import com.diving.pungdong.dto.lecture.mylist.LectureInfo;
import com.diving.pungdong.dto.lecture.newList.NewLectureInfo;
import com.diving.pungdong.dto.lecture.popularList.PopularLectureInfo;
import com.diving.pungdong.dto.lecture.search.LectureSearchResult;
import com.diving.pungdong.dto.lecture.search.SearchCondition;
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
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/lecture")
public class LectureController {

    private final LectureService lectureService;
    private final AccountService accountService;
    private final S3Uploader s3Uploader;

//    @PostMapping(value = "/create")
//    public ResponseEntity createLecture(@CurrentUser Account account,
//                                        @RequestPart("request") CreateLectureReq createLectureReq,
//                                        @RequestPart("fileList") List<MultipartFile> fileList) throws IOException {
//        Account instructor = accountService.findAccountByEmail(account.getEmail());
//
//        Lecture lecture = mapToLecture(createLectureReq, instructor);
//        List<Equipment> equipmentList = mapToEquipmentList(createLectureReq);
//
//        Lecture savedLecture = lectureService.createLecture(account.getEmail(), fileList, lecture, equipmentList);
//
//        CreateLectureRes createLectureRes
//                = new CreateLectureRes(savedLecture.getId(), savedLecture.getTitle(), savedLecture.getInstructor().getUserName());
//
//        EntityModel<CreateLectureRes> model = EntityModel.of(createLectureRes);
//        WebMvcLinkBuilder selfLink = linkTo(methodOn(LectureController.class).createLecture(account, createLectureReq, fileList));
//        model.add(selfLink.withSelfRel());
//        model.add(Link.of("/docs/api.html#resource-lecture-create").withRel("profile"));
//
//        return ResponseEntity.created(selfLink.toUri()).body(model);
//    }
//
//    public List<Equipment> mapToEquipmentList(CreateLectureReq createLectureReq) {
//        List<Equipment> equipmentList = new ArrayList<>();
//        for (EquipmentDto equipmentDto : createLectureReq.getEquipmentList()) {
//            Equipment equipment = Equipment.builder()
//                    .name(equipmentDto.getName())
//                    .price(equipmentDto.getPrice())
//                    .build();
//            equipmentList.add(equipment);
//        }
//        return equipmentList;
//    }
//
//    public Lecture mapToLecture(CreateLectureReq createLectureReq, Account instructor) {
//        return Lecture.builder()
//                .title(createLectureReq.getTitle())
//                .description(createLectureReq.getDescription())
//                .classKind(createLectureReq.getClassKind())
//                .organization(createLectureReq.getOrganization())
//                .level(createLectureReq.getLevel())
//                .price(createLectureReq.getPrice())
//                .region(createLectureReq.getRegion())
//                .instructor(instructor)
//                .build();
//    }

    @PostMapping("/create")
    public ResponseEntity<?> createLecture(@CurrentUser Account account,
                                           @Valid @RequestBody LectureCreateInfo lectureCreateInfo,
                                           BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        LectureCreateResult lectureCreateResult = lectureService.createLecture(account, lectureCreateInfo);
        EntityModel<LectureCreateResult> model = EntityModel.of(lectureCreateResult);
        model.add(linkTo(methodOn(LectureController.class).createLecture(account, lectureCreateInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-create").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/update")
    public ResponseEntity<EntityModel<LectureUpdateRes>> updateLecture(@CurrentUser Account account,
                                                                       @RequestPart("request") LectureUpdateInfo lectureUpdateInfo,
                                                                       @RequestPart("fileList") List<MultipartFile> addLectureImageFiles) throws IOException {
        Lecture lecture = lectureService.getLectureById(lectureUpdateInfo.getId());

        if (!lecture.getInstructor().getEmail().equals(account.getEmail())) {
            throw new NoPermissionsException();
        }

        Lecture updatedLecture = lectureService.updateLectureTx(account.getEmail(), lectureUpdateInfo, addLectureImageFiles, lecture);
        LectureUpdateRes lectureUpdateRes = LectureUpdateRes.builder()
                .id(updatedLecture.getId())
                .title(updatedLecture.getTitle())
                .build();

        EntityModel<LectureUpdateRes> model = EntityModel.of(lectureUpdateRes);
        model.add(linkTo(methodOn(LectureController.class).updateLecture(account, lectureUpdateInfo, addLectureImageFiles)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<EntityModel<LectureDeleteRes>> deleteLecture(@CurrentUser Account account, @RequestParam("id") Long id) {
        Lecture lecture = lectureService.getLectureById(id);
        if (!lecture.getInstructor().getEmail().equals(account.getEmail())) {
            throw new NoPermissionsException();
        }

        lectureService.deleteLectureById(id);

        LectureDeleteRes lectureDeleteRes = LectureDeleteRes.builder()
                .lectureId(id)
                .build();

        EntityModel<LectureDeleteRes> model = EntityModel.of(lectureDeleteRes);
        model.add(linkTo(methodOn(LectureController.class).deleteLecture(account, id)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

//    @GetMapping("/detail")
//    public ResponseEntity<EntityModel<LectureDetail>> getLectureDetail(@RequestParam Long id) {
//        Lecture lecture = lectureService.getLectureById(id);
//
//        LectureDetail lectureDetail = mapToLectureDetail(lecture);
//
//        EntityModel<LectureDetail> model = EntityModel.of(lectureDetail);
//        model.add(linkTo(methodOn(LectureController.class).getLectureDetail(id)).withSelfRel());
//
//        return ResponseEntity.ok().body(model);
//    }
//
//    public LectureDetail mapToLectureDetail(Lecture lecture) {
//        LectureDetail lectureDetail = LectureDetail.builder()
//                .id(lecture.getId())
//                .title(lecture.getTitle())
//                .classKind(lecture.getClassKind())
//                .organization(lecture.getOrganization())
//                .level(lecture.getLevel())
//                .description(lecture.getDescription())
//                .price(lecture.getPrice())
//                .region(lecture.getRegion())
//                .instructorId(lecture.getInstructor().getId())
//                .lectureUrlList(new ArrayList<>())
//                .equipmentList(new ArrayList<>())
//                .build();
//
//        for (LectureImage lectureImage : lecture.getLectureImages()) {
//            lectureDetail.getLectureUrlList().add(lectureImage.getFileURI());
//        }
//
//        for (Equipment equipment : lecture.getEquipmentList()) {
//            EquipmentDto equipmentDto = EquipmentDto.builder()
//                    .name(equipment.getName())
//                    .price(equipment.getPrice())
//                    .build();
//
//            lectureDetail.getEquipmentList().add(equipmentDto);
//        }
//        return lectureDetail;
//    }

    @PostMapping("/list")
    public ResponseEntity<?> searchList(@RequestBody SearchCondition searchCondition,
                                        Pageable pageable,
                                        PagedResourcesAssembler<LectureSearchResult> assembler){
        Page<Lecture> lecturePage = lectureService.searchListByCondition(searchCondition, pageable);

        List<LectureSearchResult> lectureSearchResults = mapToLectureSearchResults(lecturePage);

        Page<LectureSearchResult> result = new PageImpl<>(lectureSearchResults, pageable, lecturePage.getTotalElements());
        PagedModel<EntityModel<LectureSearchResult>> model = assembler.toModel(result);
        return ResponseEntity.ok().body(model);
    }

    public List<LectureSearchResult> mapToLectureSearchResults(Page<Lecture> lecturePage) {
        List<LectureSearchResult> lectureSearchResults = new ArrayList<>();
        for (Lecture lecture : lecturePage.getContent()) {
            List<String> imageURLs = mapToLectureImageUrls(lecture);
            LectureSearchResult lectureSearchResult = mapToLectureSearchResult(lecture, imageURLs);
            lectureSearchResults.add(lectureSearchResult);
        }
        return lectureSearchResults;
    }

    public LectureSearchResult mapToLectureSearchResult(Lecture lecture, List<String> imageURLs) {
        return LectureSearchResult.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .classKind(lecture.getClassKind())
                .organization(lecture.getOrganization())
                .level(lecture.getLevel())
                .price(lecture.getPrice())
                .region(lecture.getRegion())
                .imageURL(imageURLs)
                .build();
    }

    public List<String> mapToLectureImageUrls(Lecture lecture) {
        List<String> imageURLs = new ArrayList<>();
        for (LectureImage image : lecture.getLectureImages()) {
            imageURLs.add(image.getFileURI());
        }
        return imageURLs;
    }

    @GetMapping("/new/list")
    public ResponseEntity<?> getNewLectures(@CurrentUser Account account,
                                            Pageable pageable,
                                            PagedResourcesAssembler<NewLectureInfo> assembler) {
        Page<NewLectureInfo> lecturePage = lectureService.getNewLecturesInfo(account, pageable);

        PagedModel<EntityModel<NewLectureInfo>> model = assembler.toModel(lecturePage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/popular/list")
    public ResponseEntity<?> getPopularLectures(@CurrentUser Account account,
                                                Pageable pageable,
                                                PagedResourcesAssembler<PopularLectureInfo> assembler) {
        Page<PopularLectureInfo> lectureInfoPage = lectureService.getPopularLecturesInfo(account, pageable);

        PagedModel<EntityModel<PopularLectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/manage/list")
    public ResponseEntity<?> manageList(@CurrentUser Account account,
                                        Pageable pageable,
                                        PagedResourcesAssembler<LectureInfo> assembler) {
        Page<LectureInfo> lectureInfoPage = lectureService.getMyLectureInfoList(account, pageable);

        PagedModel<EntityModel<LectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/upload")
    public String upload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return s3Uploader.upload(file, "lecture", authentication.getName());
    }
}
