package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.domain.reservation.Reservation;
import com.diving.pungdong.dto.location.LocationCreateInfo;
import com.diving.pungdong.dto.location.LocationCreateResult;
import com.diving.pungdong.dto.location.update.LocationUpdateInfo;
import com.diving.pungdong.dto.reservation.detail.LocationDetail;
import com.diving.pungdong.repo.LocationJpaRepo;
import com.diving.pungdong.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationJpaRepo locationJpaRepo;
    private final LectureService lectureService;
    private final ReservationService reservationService;

    @Transactional
    public LocationCreateResult saveLocationWithLecture(Account account, LocationCreateInfo locationCreateInfo) {
        lectureService.checkLectureCreator(account, locationCreateInfo.getLectureId());
        Lecture lecture = lectureService.findLectureById(locationCreateInfo.getLectureId());

        Location location = Location.builder()
                .address(locationCreateInfo.getAddress())
                .latitude(locationCreateInfo.getLatitude())
                .longitude(locationCreateInfo.getLongitude())
                .build();
        locationJpaRepo.save(location);
        lecture.setLocation(location);

        return LocationCreateResult.builder()
                .lectureId(lecture.getId())
                .locationId(location.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public Location findLocationByLectureId(Long lectureId) {
        return locationJpaRepo.findByLectureId(lectureId).orElseThrow(BadRequestException::new);
    }

    public LocationDetail findByReservationId(Long reservationId) {
        Reservation reservation = reservationService.findById(reservationId);
        Location location = reservation.getSchedule().getLecture().getLocation();

        return LocationDetail.builder()
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
    }

    @Transactional
    public void updateLocationWithLecture(Account account, LocationUpdateInfo locationUpdateInfo) {
        lectureService.checkLectureCreator(account, locationUpdateInfo.getLectureId());
        Lecture lecture = lectureService.findLectureById(locationUpdateInfo.getLectureId());

        Location location = lecture.getLocation();
        location.setLatitude(locationUpdateInfo.getLatitude());
        location.setLongitude(locationUpdateInfo.getLongitude());
        location.setAddress(locationUpdateInfo.getAddress());

        locationJpaRepo.save(location);
    }
}
