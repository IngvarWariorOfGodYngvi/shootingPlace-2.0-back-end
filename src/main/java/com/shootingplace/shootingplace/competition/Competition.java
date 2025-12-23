package com.shootingplace.shootingplace.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    public Integer getPracticeShots() {
        return practiceShots;
    }

    public void setPracticeShots(Integer practiceShots) {
        this.practiceShots = practiceShots;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public String getCountingMethod() {
        return countingMethod;
    }

    public void setCountingMethod(String countingMethod) {
        this.countingMethod = countingMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
//
//    public String getDiscipline() {
//        return discipline;
//    }
//
//    public void setDiscipline(String discipline) {
//        this.discipline = discipline;
//    }

    public Integer getNumberOfShots() {
        return numberOfShots;
    }

    public void setNumberOfShots(Integer numberOfShots) {
        this.numberOfShots = numberOfShots;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

//    public String[] getDisciplines() {
//        return disciplines;
//    }
//
//    public void setDisciplines(String[] disciplines) {
//        this.disciplines = disciplines;
//    }
//
//    public Integer[] getNumberOfManyShots() {
//        return numberOfManyShots;
//    }
//
//    public void setNumberOfManyShots(Integer[] numberOfManyShots) {
//        this.numberOfManyShots = numberOfManyShots;
//    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
