package com.shootingplace.shootingplace.competition;

import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Competition {

    private String name;
    private String abbreviation;

    private String disciplineList;

    private Integer numberOfShots;

    private String type;

    private String countingMethod;

    private Integer ordering;

    private Integer practiceShots;

    private String caliberUUID;

    public List<String> getDisciplineList() {
        List<String> vals = new ArrayList<>();
        if (disciplineList != null) {
            vals.addAll(Arrays.asList(disciplineList.split(";")));
        }
        return vals;
    }

    public void setDisciplineList(List<String> disciplineList) {
        String value = "";
        if (disciplineList == null || disciplineList.isEmpty()) {
            this.disciplineList = null;
        } else {
            for (String f : disciplineList) {
                value = value.concat(f + ";");
            }
            this.disciplineList = value;
        }
    }
}
