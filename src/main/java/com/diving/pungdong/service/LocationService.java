package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.dto.location.LocationCreateInfo;
import com.diving.pungdong.dto.location.LocationCreateResult;
import com.diving.pungdong.repo.LocationJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationJpaRepo locationJpaRepo;
    private final LectureService lectureService;

    @Transactional
    public LocationCreateResult saveLocationWithLecture(Account account, LocationCreateInfo locationCreateInfo) {
        lectureService.checkLectureCreator(account, locationCreateInfo.getLectureId());
        Lecture lecture = lectureService.getLectureById(locationCreateInfo.getLectureId());

        Location location = Location.builder()
                .address(locationCreateInfo.getAddress())
                .latitude(locationCreateInfo.getLatitude())
                .longitude(locationCreateInfo.getLongitude())
                .lecture(lecture)
                .build();

        locationJpaRepo.save(location);
        return LocationCreateResult.builder()
                .lectureId(lecture.getId())
                .locationId(location.getId())
                .build();
    }
}
