package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherPerson {

    private String id;
    private String firstName;
    private String secondName;
    private String phoneNumber;
    private String email;
    private Address address;
    private MemberPermissions memberPermissions;

    private Club club;
    private String weaponPermissionNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getWeaponPermissionNumber() {
        return weaponPermissionNumber;
    }

    public void setWeaponPermissionNumber(String weaponPermissionNumber) {
        this.weaponPermissionNumber = weaponPermissionNumber;
    }

    public MemberPermissions getMemberPermissions() {
        return memberPermissions;
    }

    public void setMemberPermissions(MemberPermissions memberPermissions) {
        this.memberPermissions = memberPermissions;
    }
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }
    @Override
    public String toString() {
        return "OtherPerson{" +
                "firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", address=" + address +
                ", memberPermissions=" + memberPermissions +
                ", club=" + club +
                ", weaponPermissionNumber='" + weaponPermissionNumber + '\'' +
                '}';
    }
}
