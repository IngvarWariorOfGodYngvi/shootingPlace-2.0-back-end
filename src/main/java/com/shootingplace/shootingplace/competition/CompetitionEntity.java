package com.shootingplace.shootingplace.competition;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionEntity {
    @Id
    @UuidGenerator
    private String uuid;

    private String name;
    private String abbreviation;

    private Integer numberOfShots;

    private String disciplineList;

    private String numberOfManyShotsList;

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

    public List<String> getNumberOfManyShotsList() {
        List<String> vals = new ArrayList<>();
        if (numberOfManyShotsList != null) {
            vals.addAll(Arrays.asList(numberOfManyShotsList.split(";")));
        }
        return vals;
    }

    public void setNumberOfManyShotsList(List<String> numberOfManyShotsList) {
        String value = "";
        for (String f : numberOfManyShotsList) {
            value = value.concat(f + ";");
        }
        this.numberOfManyShotsList = value;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public Integer getPracticeShots() {
        return practiceShots;
    }

    public void setPracticeShots(Integer practiceShots) {
        this.practiceShots = practiceShots;
    }

    public Integer getNumberOfShots() {
        return numberOfShots;
    }

    public void setNumberOfShots(Integer numberOfShots) {
        this.numberOfShots = numberOfShots;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountingMethod() {
        return countingMethod;
    }

    public void setCountingMethod(String countingMethod) {
        this.countingMethod = countingMethod;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

}
