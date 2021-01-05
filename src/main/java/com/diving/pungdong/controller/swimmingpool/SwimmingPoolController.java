package com.diving.pungdong.controller.swimmingpool;

import com.diving.pungdong.controller.lecture.LectureController;
import com.diving.pungdong.domain.swimmingPool.Location;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.service.SwimmingPoolService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/swimmingPool", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class SwimmingPoolController {

    private final SwimmingPoolService swimmingPoolService;

    @PostMapping("/create")
    public ResponseEntity saveSwimmingPool(@RequestBody SwimmingPoolSaveReq saveReq) {
        SwimmingPool swimmingPool = SwimmingPool.builder().location(saveReq.getLocation()).build();
        swimmingPoolService.saveSwimmingPool(swimmingPool);
        WebMvcLinkBuilder selfLink = linkTo(methodOn(SwimmingPoolController.class).saveSwimmingPool(saveReq));

        return ResponseEntity.created(selfLink.toUri()).build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class SwimmingPoolSaveReq {
        private Location location;
    }
}
