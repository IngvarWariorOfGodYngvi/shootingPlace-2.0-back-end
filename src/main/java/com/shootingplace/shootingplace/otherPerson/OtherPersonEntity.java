package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @OneToOne(orphanRemoval = true)
    private AddressEntity address;
    @ManyToOne
    private ClubEntity club;
    @OneToOne(orphanRemoval = true)
    private MemberPermissionsEntity permissionsEntity;

    private LocalDateTime creationDate;

    public Integer getId() {
        return id;
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
        this.secondName = secondName.toUpperCase();
    }

    public ClubEntity getClub() {
        return club;
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    public MemberPermissionsEntity getPermissionsEntity() {
        return permissionsEntity;
    }

    public void setPermissionsEntity(MemberPermissionsEntity permissionsEntity) {
        this.permissionsEntity = permissionsEntity;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getWeaponPermissionNumber() {
        return weaponPermissionNumber;
    }

    public void setWeaponPermissionNumber(String weaponPermissionNumber) {
        this.weaponPermissionNumber = weaponPermissionNumber;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    public void setCreationDate() {
        this.creationDate = LocalDateTime.now();
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    /**
     * Return secondName plus firstName of OtherPerson
     */
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }
}
