package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Getter
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

    public void setAmmoList(List<AmmoUsedEntity> ammoList) {
        this.ammoList = ammoList;
    }

    @Override
    public String toString() {
        return "PersonalEvidenceEntity{" +
                "ammoList=" + ammoList +
                '}';
    }
}
