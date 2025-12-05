package com.shootingplace.shootingplace.history;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionHistoryEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String name;

    private String attachedToList;

    private String discipline;

    private String[] disciplines;

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

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }
    public List<String> getDisciplineList() {
        List<String> vals = new ArrayList<>();
        if (disciplineList != null) {
            for (String s : disciplineList.split(";")) {
                vals.add(String.valueOf(s));
            }
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

    public String[] getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(String[] disciplines) {
        this.disciplines = disciplines;
    }
}
