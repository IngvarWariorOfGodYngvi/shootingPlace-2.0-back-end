package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalEvidence {

    private List<AmmoUsedEntity> ammoList;

}
