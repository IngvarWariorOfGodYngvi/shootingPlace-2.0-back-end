package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.permissions.MemberPermissionsEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.validators.ValidPESEL;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MemberEntity extends Person {

    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String clubCardBarCode;
    @OneToMany
    private List<BarCodeCardEntity> barCodeCardList;
    @NotBlank
    private String firstName;
    @NotBlank
    private String secondName;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LicenseEntity license;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ShootingPatentEntity shootingPatent;
    @Email
    private String email;
    @NotBlank
    @ValidPESEL
    @Pattern(regexp = "[0-9]*")
    private String pesel;
    @NotBlank
    private String IDCard;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ClubEntity club;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AddressEntity address;
    @NotBlank
    @Pattern(regexp = "^\\+[0-9]{11}$")
    private String phoneNumber;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WeaponPermissionEntity weaponPermission;

    private String note;

    private String imageUUID;

    private String signBy;
    private boolean active;
    private boolean adult;
    private boolean erased;
    private boolean pzss;
    private boolean declarationLOK;

    @Nullable
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ErasedEntity erasedEntity;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HistoryEntity history;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberPermissionsEntity memberPermissions;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PersonalEvidenceEntity personalEvidence;
    @ManyToOne
    private MemberGroupEntity memberGroupEntity;

    /**
     * Return secondName plus firstName of Member
     */
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }


    /**
     * Return member Sex
     * if false -> man
     * if true -> woman
     */
    public boolean getSex() {
        return Integer.parseInt(String.valueOf(this.pesel.toCharArray()[10])) % 2 == 0;
    }

    public LocalDate getBirthDate() {
        return LocalDate.of(getBirthYear(), getBirthMonth(), getBirthDay());
    }

    private int getBirthYear() {
        int[] PESEL = getInts();
        int year;
        int month;
        year = 10 * PESEL[0];
        year += PESEL[1];
        month = 10 * PESEL[2];
        month += PESEL[3];
        if (month > 80 && month < 93) {
            year += 1800;
        } else if (month > 0 && month < 13) {
            year += 1900;
        } else if (month > 20 && month < 33) {
            year += 2000;
        } else if (month > 40 && month < 53) {
            year += 2100;
        } else if (month > 60 && month < 73) {
            year += 2200;
        }
        return year;

    }

    private int getBirthMonth() {
        int[] PESEL = getInts();
        int month;
        month = 10 * PESEL[2];
        month += PESEL[3];
        if (month > 80 && month < 93) {
            month -= 80;
        } else if (month > 20 && month < 33) {
            month -= 20;
        } else if (month > 40 && month < 53) {
            month -= 40;
        } else if (month > 60 && month < 73) {
            month -= 60;
        }
        return month;
    }

    private int[] getInts() {
        char[] chars = this.pesel.toCharArray();
        int[] PESEL = new int[11];
        for (int i = 0; i < chars.length; i++) {
            PESEL[i] = Integer.parseInt(String.valueOf(chars[i]));
        }
        return PESEL;
    }

    private int getBirthDay() {
        int[] PESEL = getInts();
        int day;
        day = 10 * PESEL[4];
        day += PESEL[5];
        return day;
    }
}
