package com.diving.pungdong.controller.equipment;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.EquipmentStock;
import com.diving.pungdong.dto.equipment.stock.create.EquipmentStockCreateInfo;
import com.diving.pungdong.service.EquipmentStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/equipment-stock")
public class EquipmentStockController {
    private final EquipmentStockService equipmentStockService;

    @PostMapping
    public ResponseEntity<?> addLectureEquipmentStock(@CurrentUser Account account,
                                                      @Valid @RequestBody EquipmentStockCreateInfo stockCreateInfo,
                                                      BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        EquipmentStock equipmentStock = equipmentStockService.createEquipmentStock(account, stockCreateInfo);

        return ResponseEntity.created(linkTo(EquipmentStockController.class).slash(equipmentStock.getId()).toUri()).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeEquipmentStock(@CurrentUser Account account,
                                                  @PathVariable("id") Long id) {
        equipmentStockService.deleteEquipmentStock(account, id);

        return ResponseEntity.noContent().build();
    }
}
