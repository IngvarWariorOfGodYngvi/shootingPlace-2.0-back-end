package com.shootingplace.shootingplace.history;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionHistoryEntity {
    @Id
    @UuidGenerator
    private String uuid;

    private String name;

    private String attachedToList;

    private String disciplineList;

    private LocalDate date;

    private boolean WZSS;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttachedToList() {
        return attachedToList;
    }

    public void setAttachedToList(String attachedToList) {
        this.attachedToList = attachedToList;
    }

    public List<String> getDisciplineList() {
        List<String> vals = new ArrayList<>();
        if (disciplineList != null) {
            vals.addAll(Arrays.asList(disciplineList.split(";")));
        }
        return vals;
    }

    public void setDisciplineList(List<String> disciplineList) {
        String value = "";
        for (String f : disciplineList) {
            value = value.concat(f + ";");
        }
        this.disciplineList = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isWZSS() {
        return WZSS;
    }

    public void setWZSS(boolean WZSS) {
        this.WZSS = WZSS;
    }

}
