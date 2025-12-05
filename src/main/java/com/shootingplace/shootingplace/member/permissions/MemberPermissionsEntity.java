package com.shootingplace.shootingplace.member.permissions;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberPermissionsEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String instructorNumber;

    private String shootingLeaderNumber;

    private String arbiterNumber;

    private String arbiterClass;
    private LocalDate arbiterPermissionValidThru;

    public String getUuid() {
        return uuid;
    }

    public String getInstructorNumber() {
        return instructorNumber;
    }

    public void setInstructorNumber(String instructorNumber) {
        this.instructorNumber = instructorNumber;
    }

    public String getShootingLeaderNumber() {
        return shootingLeaderNumber;
    }

    public void setShootingLeaderNumber(String shootingLeaderNumber) {
        this.shootingLeaderNumber = shootingLeaderNumber;
    }

    public String getArbiterNumber() {
        return arbiterNumber;
    }

    public void setArbiterNumber(String arbiterNumber) {
        this.arbiterNumber = arbiterNumber;
    }

    public String getArbiterClass() {
        return arbiterClass;
    }

    public void setArbiterClass(String arbiterClass) {
        this.arbiterClass = arbiterClass;
    }

    public LocalDate getArbiterPermissionValidThru() {
        return arbiterPermissionValidThru;
    }

    public void setArbiterPermissionValidThru(LocalDate arbiterPermissionValidThru) {
        this.arbiterPermissionValidThru = arbiterPermissionValidThru;
    }
}
