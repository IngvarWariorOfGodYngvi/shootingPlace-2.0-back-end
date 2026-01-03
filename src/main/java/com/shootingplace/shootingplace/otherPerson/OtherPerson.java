package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import lombok.*;
@Data
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
    private String licenseNumber;
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }
}
