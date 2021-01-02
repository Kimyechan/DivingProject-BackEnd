package com.diving.pungdong.domain.swimmingPool;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Getter
@Setter
@Embeddable
public class Location {
    Double latitude;
    Double longitude;
}
