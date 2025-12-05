package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.validators.ValidPESEL;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberEntity extends Person {

    @Id
    @GeneratedValue
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
    private String email ;
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
    @ManyToOne(fetch = FetchType.LAZY)
    private MemberGroupEntity memberGroup;
    private String note;

    private String imageUUID;

    private String signBy;
    private Boolean active;
    private Boolean adult;
    private Boolean erased;
    private Boolean pzss;
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

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getLegitimationNumber() {
        return legitimationNumber;
    }

    public String getClubCardBarCode() {
        return clubCardBarCode;
    }

    public void setClubCardBarCode(String clubCardBarCode) {
        this.clubCardBarCode = clubCardBarCode;
    }

    public void setLegitimationNumber(Integer legitimationNumber) {
        this.legitimationNumber = legitimationNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public LicenseEntity getLicense() {
        return license;
    }

    public void setLicense(LicenseEntity license) {
        this.license = license;
    }

    public ShootingPatentEntity getShootingPatent() {
        return shootingPatent;
    }

    public void setShootingPatent(ShootingPatentEntity shootingPatent) {
        this.shootingPatent = shootingPatent;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    public String getIDCard() {
        return IDCard;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public WeaponPermissionEntity getWeaponPermission() {
        return weaponPermission;
    }

    public void setWeaponPermission(WeaponPermissionEntity weaponPermission) {
        this.weaponPermission = weaponPermission;
    }

    public ErasedEntity getErasedEntity() {
        return erasedEntity;
    }

    public void setErasedEntity(ErasedEntity erasedEntity) {
        this.erasedEntity = erasedEntity;
    }

    public List<BarCodeCardEntity> getBarCodeCardList() {
        return barCodeCardList;
    }

    public void setBarCodeCardList(List<BarCodeCardEntity> barCodeCardList) {
        this.barCodeCardList = barCodeCardList;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public Boolean getAdult() {
        return adult;
    }

    public Boolean getErased() {
        return erased;
    }

    public void toggleErase() {
        this.erased = !this.erased;
    }

    public HistoryEntity getHistory() {
        return history;
    }

    public void setHistory(HistoryEntity history) {
        this.history = history;
    }

    public MemberPermissionsEntity getMemberPermissions() {
        return memberPermissions;
    }

    public void setMemberPermissions(MemberPermissionsEntity memberPermissions) {
        this.memberPermissions = memberPermissions;
    }

    public PersonalEvidenceEntity getPersonalEvidence() {
        return personalEvidence;
    }

    public void setPersonalEvidence(PersonalEvidenceEntity personalEvidence) {
        this.personalEvidence = personalEvidence;
    }

    public ClubEntity getClub() {
        return club;
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Boolean getPzss() {
        return pzss;
    }

    public void setPzss(Boolean pzss) {
        this.pzss = pzss;
    }

    public String getSignBy() {
        return signBy;
    }

    public void setSignBy(String signBy) {
        this.signBy = signBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Return secondName plus firstName of Member
     */
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }

    public boolean getDeclarationLOK() {
        return declarationLOK;
    }

    public boolean toggleDeclaration(boolean isSigned) {
       return this.declarationLOK = isSigned;
    }
    /**
    * Return member Sex
     * if false -> man
     * if true -> woman
    */
    public boolean getSex() {
        return Integer.parseInt(String.valueOf(this.pesel.toCharArray()[10])) % 2 == 0;
    }

    public boolean togglePzss(boolean isSignedTo) {
        return this.pzss = isSignedTo;
    }

    public LocalDate getBirthDate(){
       return LocalDate.of(getBirthYear(),getBirthMonth(),getBirthDay());
    }

    public MemberGroupEntity getMemberGroup() {
        return memberGroup;
    }

    public void setMemberGroup(MemberGroupEntity memberGroup) {
        this.memberGroup = memberGroup;
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
        for (int i =0; i<chars.length;i++){
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
