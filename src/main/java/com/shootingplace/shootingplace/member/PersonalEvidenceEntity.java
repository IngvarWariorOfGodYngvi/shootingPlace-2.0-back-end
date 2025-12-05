package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalEvidenceEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    @OneToMany
    @OrderBy("caliberName ASC")
    private List<AmmoUsedEntity> ammoList;

    public String getUuid() {
        return uuid;
    }

    public List<AmmoUsedEntity> getAmmoList() {
        return ammoList;
    }

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
