package com.shootingplace.shootingplace.history;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgingHistoryEntity {

    @Id
    @UuidGenerator
    private String uuid;
    private String name;

    private String tournamentUUID;
    private String judgingFunction;
    private LocalDate date;
    private LocalTime time;

    public void setName(String name) {
        this.name = name;
    }

    public void setTournamentUUID(String tournamentUUID) {
        this.tournamentUUID = tournamentUUID;
    }

    public void setJudgingFunction(String judgingFunction) {
        this.judgingFunction = judgingFunction;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "JudgingHistoryEntity{" +
                "name='" + name + '\'' +
                ", tournamentUUID='" + tournamentUUID + '\'' +
                ", judgingFunction='" + judgingFunction + '\'' +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
