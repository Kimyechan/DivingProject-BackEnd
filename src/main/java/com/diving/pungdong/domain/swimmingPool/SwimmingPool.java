package com.diving.pungdong.domain.swimmingPool;

import com.diving.pungdong.domain.Location;
import lombok.*;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwimmingPool {

    @Id @GeneratedValue
    private Long id;

    @Embedded
    private Location location;
}
