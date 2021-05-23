package com.diving.pungdong.controller.location;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.location.LocationCreateInfo;
import com.diving.pungdong.dto.location.LocationCreateResult;
import com.diving.pungdong.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
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
@RequestMapping(value = "/location")
public class LocationController {
    private final LocationService locationService;

    @PostMapping("/create")
    public ResponseEntity<?> createLocationOfLecture(@CurrentUser Account account,
                                                     @Valid @RequestBody LocationCreateInfo locationCreateInfo,
                                                     BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        LocationCreateResult locationCreateResult = locationService.saveLocationWithLecture(account, locationCreateInfo);

        EntityModel<LocationCreateResult> model = EntityModel.of(locationCreateResult);
        WebMvcLinkBuilder location = linkTo(methodOn(LocationController.class).createLocationOfLecture(account, locationCreateInfo, result));
        model.add(location.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-location-create").withRel("profile"));
        return ResponseEntity.created(location.toUri()).body(model);
    }
}
