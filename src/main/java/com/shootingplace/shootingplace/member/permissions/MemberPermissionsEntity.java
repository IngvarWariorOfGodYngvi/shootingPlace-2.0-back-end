package com.shootingplace.shootingplace.member.permissions;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberPermissionsEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String instructorNumber;

    private String shootingLeaderNumber;

    private String arbiterStaticNumber;
    private String arbiterStaticClass;
    private LocalDate arbiterStaticPermissionValidThru;

    private String arbiterDynamicNumber;
    private String arbiterDynamicClass;
    private LocalDate arbiterDynamicPermissionValidThru;
    
}
