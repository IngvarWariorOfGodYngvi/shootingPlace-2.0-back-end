package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalEvidence {

    private List<AmmoUsedEntity> ammoList;

    public List<AmmoUsedEntity> getAmmoList() {
        return ammoList;
    }

    public void setAmmoList(List<AmmoUsedEntity> ammoList) {
        this.ammoList = ammoList;
    }

}
