package com.diving.pungdong.dto.reservation.list;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.domain.reservation.Reservation;
import lombok.Builder;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Getter
public class PastReservationUIModel extends RepresentationModel<PastReservationUIModel> {
    private final Long reservationId;
    private final LocalDate reservationDate;
    private final String lectureTitle;
    private final Organization organization;
    private final String level;
    private final String lectureImageUrl;
    private final String instructorNickname;
    private final Boolean isExistedReview;

    @Builder
    public PastReservationUIModel(Reservation reservation,
                                  Lecture lecture,
                                  String lectureImageUrl,
                                  String instructorNickname,
                                  Boolean isExistedReview) {
        this.reservationId = reservation.getId();
        this.reservationDate = reservation.getDateOfReservation();
        this.lectureTitle = lecture.getTitle();
        this.organization = lecture.getOrganization();
        this.level = lecture.getLevel();
        this.lectureImageUrl = lectureImageUrl;
        this.instructorNickname = instructorNickname;
        this.isExistedReview = isExistedReview;
    }
}
