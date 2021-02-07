package com.diving.pungdong.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    private String address;
}
