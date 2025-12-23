package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.enums.ErasedType;
import com.shootingplace.shootingplace.license.License;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import lombok.*;

import java.time.LocalDate;
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO extends Person {
    private String uuid;
    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String firstName;
    private String secondName;
    private String email;
    private ErasedType erasedType;
    private Erased erasedEntity;
    private License license;
    private boolean pzss;
    private MemberPermissions memberPermissions;
    private String image;
    private boolean adult;
    private boolean active;
    private boolean erased;
    private Club club;
    private boolean declarationLOK;
    private String note;
    private String group;
    /**
     * Return secondName plus firstName of Member
     */
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }

}
