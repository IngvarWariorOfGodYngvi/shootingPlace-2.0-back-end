package com.shootingplace.shootingplace.member.permissions;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberPermissions {

    private String instructorNumber;

    private String shootingLeaderNumber;

    private String arbiterStaticNumber;

    private String arbiterStaticClass;
    private LocalDate arbiterStaticPermissionValidThru;

}