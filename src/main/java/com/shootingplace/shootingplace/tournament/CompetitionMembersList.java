package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.score.Score;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionMembersList {

    private String uuid;
    private String name;
    private String attachedToTournament;
    private LocalDate date;

    private String disciplineList;
    private Integer numberOfShots;
    private String numberOfManyShotsList;


    private String type;

    private String countingMethod;

    private boolean WZSS;

    private Integer ordering;

    private Integer practiceShots;

    private String caliberUUID;

    @ManyToMany
    private List<Score> scoreList;

    public Integer getScoreListSize() {
        return scoreListSize;
    }

    private Integer scoreListSize;

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

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttachedToTournament() {
        return attachedToTournament;
    }

    public void setAttachedToTournament(String attachedToTournament) {
        this.attachedToTournament = attachedToTournament;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public List<Score> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<Score> scoreList) {
        this.scoreList = scoreList;
    }

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

    public String getCountingMethod() {
        return countingMethod;
    }

    public void setCountingMethod(String countingMethod) {
        this.countingMethod = countingMethod;
    }

    public boolean isWZSS() {
        return WZSS;
    }

    public void setWZSS(boolean WZSS) {
        this.WZSS = WZSS;
    }

    public void setDisciplineList(List<String> disciplineList) {
        String value = "";
        for (String f : disciplineList) {
            value = value.concat(f + ";");
        }
        this.disciplineList = value;
    }

    public List<String> getDisciplineList() {
        List<String> vals = new ArrayList<>();
        if (disciplineList != null) {
            vals.addAll(Arrays.asList(disciplineList.split(";")));
        }
        return vals;
    }

    public void setDisciplineList(String disciplineList) {
        this.disciplineList = disciplineList;
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

    @Override
    public String toString() {
        return "CompetitionMembersList{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", attachedToTournament='" + attachedToTournament + '\'' +
                ", date=" + date +
                ", disciplineList='" + disciplineList + '\'' +
                ", numberOfShots=" + numberOfShots +
                ", numberOfManyShotsList='" + numberOfManyShotsList + '\'' +
                ", type='" + type + '\'' +
                ", countingMethod='" + countingMethod + '\'' +
                ", WZSS=" + WZSS +
                ", ordering=" + ordering +
                ", practiceShots=" + practiceShots +
                ", caliberUUID='" + caliberUUID + '\'' +
                ", scoreList=" + scoreList +
                ", scoreListSize=" + scoreListSize +
                '}';
    }
}
