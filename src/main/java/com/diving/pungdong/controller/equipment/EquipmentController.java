package com.diving.pungdong.controller.equipment;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.equipment.create.EquipmentCreateInfo;
import com.diving.pungdong.dto.equipment.create.EquipmentCreateResult;
import com.diving.pungdong.service.EquipmentService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;
    private final LectureEsService lectureEsService;

    @PostMapping("/create/list")
    public ResponseEntity<?> createLectureEquipments(@CurrentUser Account account,
                                                     @Valid @RequestBody EquipmentCreateInfo equipmentCreateInfo,
                                                     BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        EquipmentCreateResult equipmentCreateResult = equipmentService.saveRentEquipmentInfos(account, equipmentCreateInfo);
        lectureEsService.saveLectureInfo(equipmentCreateResult.getLectureId());

        EntityModel<EquipmentCreateResult> model = EntityModel.of(equipmentCreateResult);
        WebMvcLinkBuilder location = linkTo(methodOn(EquipmentController.class).createLectureEquipments(account, equipmentCreateInfo, result));
        model.add(location.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-equipment-list-create").withRel("profile"));
        return ResponseEntity.created(location.toUri()).body(model);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeLectureEquipment(@CurrentUser Account account,
                                                    @PathVariable("id") Long id) {
        equipmentService.deleteLectureEquipment(account, id);

        return ResponseEntity.noContent().build();
    }
}
