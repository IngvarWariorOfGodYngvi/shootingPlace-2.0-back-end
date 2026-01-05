package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.permissions.MemberPermissionsEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherPersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String secondName;
    private String phoneNumber;
    private String email;
    private String weaponPermissionNumber;
    private boolean active;
    private String licenseNumber;
    @OneToOne(orphanRemoval = true)
    private AddressEntity address;
    @ManyToOne
    private ClubEntity club;
    @OneToOne(orphanRemoval = true)
    private MemberPermissionsEntity permissionsEntity;

    private LocalDateTime creationDate;

    public void setFirstName(String firstName) {
        this.firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName.toUpperCase();
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    public void setPermissionsEntity(MemberPermissionsEntity permissionsEntity) {
        this.permissionsEntity = permissionsEntity;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setWeaponPermissionNumber(String weaponPermissionNumber) {
        this.weaponPermissionNumber = weaponPermissionNumber;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    public void setCreationDate() {
        this.creationDate = LocalDateTime.now();
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    /**
     * Return secondName plus firstName of OtherPerson
     */
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }
}
