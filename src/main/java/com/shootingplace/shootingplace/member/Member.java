package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardDTO;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.history.History;
import com.shootingplace.shootingplace.license.License;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatent;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermission;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Member extends Person {
    private String uuid;
    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String firstName;
    private String secondName;
    private License license;
    private ShootingPatent shootingPatent;
    private String email ;

    private String pesel;
    private String IDCard;
    private ClubEntity club;
    private Address address;
    private String phoneNumber;
    private String image;
    private WeaponPermission weaponPermission;

    private Boolean active;
    private Boolean adult;
    private Boolean erased;

    private History history;

    private MemberPermissions memberPermissions;

    private PersonalEvidence personalEvidence;
    private boolean pzss;
    private ErasedEntity erasedEntity;
    private String note;
    private String group;

    private List<BarCodeCardDTO> barCodeCardList;

    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }


}
