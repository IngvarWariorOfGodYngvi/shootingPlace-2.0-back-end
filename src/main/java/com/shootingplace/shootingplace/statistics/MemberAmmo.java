package com.shootingplace.shootingplace.statistics;

import com.shootingplace.shootingplace.armory.Caliber;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAmmo {

    private String uuid;
    private String firstName;
    private String secondName;
    private Integer legitimationNumber;
    private List<Caliber> caliber;

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
