package com.shootingplace.shootingplace.member.permissions;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
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

    private String arbiterNumber;

    private String arbiterClass;
    private LocalDate arbiterPermissionValidThru;

    public void setInstructorNumber(String instructorNumber) {
        this.instructorNumber = instructorNumber;
    }

    public void setShootingLeaderNumber(String shootingLeaderNumber) {
        this.shootingLeaderNumber = shootingLeaderNumber;
    }

    public void setArbiterNumber(String arbiterNumber) {
        this.arbiterNumber = arbiterNumber;
    }

    public void setArbiterClass(String arbiterClass) {
        this.arbiterClass = arbiterClass;
    }

    public void setArbiterPermissionValidThru(LocalDate arbiterPermissionValidThru) {
        this.arbiterPermissionValidThru = arbiterPermissionValidThru;
    }
}
