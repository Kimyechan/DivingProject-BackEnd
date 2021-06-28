package com.diving.pungdong.controller.location;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.dto.location.LocationCreateInfo;
import com.diving.pungdong.dto.location.LocationCreateResult;
import com.diving.pungdong.dto.location.LocationInfo;
import com.diving.pungdong.dto.location.update.LocationUpdateInfo;
import com.diving.pungdong.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/location")
public class LocationController {
    private final LocationService locationService;
    private final ModelMapper modelMapper;

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

    @GetMapping
    public ResponseEntity<?> findLocationOfLecture(@NotNull @RequestParam Long lectureId) {
        Location location = locationService.findLocationByLectureId(lectureId);
        LocationInfo locationInfo = modelMapper.map(location, LocationInfo.class);

        EntityModel<LocationInfo> model = EntityModel.of(locationInfo);
        model.add(linkTo(methodOn(LocationController.class).findLocationOfLecture(lectureId)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-location-find").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @PutMapping
    public ResponseEntity<?> updateLocationOfLecture(@CurrentUser Account account,
                                                     @Valid @RequestBody LocationUpdateInfo locationUpdateInfo,
                                                     BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        locationService.updateLocationWithLecture(account, locationUpdateInfo);

        return ResponseEntity.noContent().build();
    }
}