package com.shootingplace.shootingplace.statistics;

import com.shootingplace.shootingplace.armory.Caliber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAmmo {

    private String uuid;
    private String firstName;
    private String secondName;
    private Integer legitimationNumber;
    private List<Caliber> caliber;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public List<Caliber> getCaliber() {
        return caliber;
    }

    public void setCaliber(List<Caliber> caliber) {
        this.caliber = caliber;
    }

    public Integer getLegitimationNumber() {
        return legitimationNumber;
    }

    public void setLegitimationNumber(Integer legitimationNumber) {
        this.legitimationNumber = legitimationNumber;
    }

    @Override
    public String toString() {
        return "MemberAmmo{" +
                "uuid='" + uuid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", legitimationNumber=" + legitimationNumber +
                ", caliber=" + caliber +
                '}';
    }
}
