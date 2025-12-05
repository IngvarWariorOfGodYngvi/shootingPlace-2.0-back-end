package com.shootingplace.shootingplace.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MemberInfo {

    String uuid;
    String secondName;
    String firstName;
    String name;
    String arbiterClass;
    int id;
    Integer legitimationNumber;
    boolean isActive;
    boolean isAdult;
    boolean declarationLOK;
    String image;
    String note;
}
