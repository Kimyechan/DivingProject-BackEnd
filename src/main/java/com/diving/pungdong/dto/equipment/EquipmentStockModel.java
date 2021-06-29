package com.diving.pungdong.dto.equipment;

import com.diving.pungdong.controller.equipment.EquipmentStockController;
import com.diving.pungdong.domain.equipment.EquipmentStock;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Getter
public class EquipmentStockModel extends RepresentationModel<EquipmentStockModel> {
    private final EquipmentStockDto equipmentStockDto;

    public EquipmentStockModel(EquipmentStock equipmentStock) {
        this.equipmentStockDto = EquipmentStockDto.builder()
                .id(equipmentStock.getId())
                .size(equipmentStock.getSize())
                .quantity(equipmentStock.getQuantity())
                .build();

        add(linkTo(EquipmentStockController.class).slash(equipmentStock.getId()).withSelfRel());
    }
}
