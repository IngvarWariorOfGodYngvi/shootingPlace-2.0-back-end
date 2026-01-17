package com.shootingplace.shootingplace.score;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ScoreEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private float score;
    private String series;

    private float alfa;
    private float charlie;
    private float delta;
    private float miss;

    private float innerTen;
    private float outerTen;
    private float hf;

    private int procedures;

    private String name;

    private int metricNumber;

    private boolean ammunition;
    private boolean gun;

    private boolean dnf;
    private boolean dsq;
    private boolean pk;
    private boolean edited;
    private LocalDateTime createDate;

    private String competitionMembersListEntityUUID;
    @ManyToOne
    private MemberEntity member;
    @ManyToOne
    private OtherPersonEntity otherPersonEntity;

    public List<Float> getSeries() {
        List<Float> vals = new ArrayList<>();
        if (series != null && !series.isEmpty()) {
            for (String s : series.split(";")) {
                vals.add(Float.valueOf(s));
            }
        }
        return vals;
    }

    public void setSeries(List<Float> series) {
        String value = "";
        for (Float f : series) {
            value = value.concat(f + ";");
        }
        this.series = value;
    }

    @Override
    public String toString() {
        return "ScoreEntity{" +
                "uuid='" + uuid + '\'' +
                ", score=" + score +
                ", series='" + series + '\'' +
                ", alfa=" + alfa +
                ", charlie=" + charlie +
                ", delta=" + delta +
                ", miss=" + miss +
                ", innerTen=" + innerTen +
                ", outerTen=" + outerTen +
                ", hf=" + hf +
                ", procedures=" + procedures +
                ", name='" + name + '\'' +
                ", metricNumber=" + metricNumber +
                ", ammunition=" + ammunition +
                ", gun=" + gun +
                ", dnf=" + dnf +
                ", dsq=" + dsq +
                ", pk=" + pk +
                ", edited=" + edited +
                ", createDate=" + createDate +
                ", competitionMembersListEntityUUID='" + competitionMembersListEntityUUID + '\'' +
                ", member=" + member +
                ", otherPersonEntity=" + otherPersonEntity +
                '}';
    }
}
