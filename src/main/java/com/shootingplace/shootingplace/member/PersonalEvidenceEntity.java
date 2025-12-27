package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalEvidenceEntity {

    @Id
    @UuidGenerator
    private String uuid;

    @OneToMany
    @OrderBy("caliberName ASC")
    private List<AmmoUsedEntity> ammoList;

}
