package com.diving.pungdong.domain.lecture;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileURI;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @PrePersist
    public void prePersist() {
        this.fileURI = this.fileURI == null ? "" : this.fileURI;
    }
}
