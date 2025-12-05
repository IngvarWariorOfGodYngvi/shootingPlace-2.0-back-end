package com.shootingplace.shootingplace.score;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreEntity {

    @Id
    @GeneratedValue
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
    @OneToOne(orphanRemoval = true)
    private MemberEntity member;
    @OneToOne(orphanRemoval = true)
    private OtherPersonEntity otherPersonEntity;

    public String getUuid() {
        return uuid;
    }

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
        this.edited = true;
    }

    public boolean isDnf() {
        return dnf;
    }

    public void setDnf(boolean dnf) {
        this.dnf = dnf;
    }

    public boolean isDsq() {
        return dsq;
    }

    public void setDsq(boolean dsq) {
        this.dsq = dsq;
    }

    public float getScore() {
        return score;
    }

    public float getInnerTen() {
        return innerTen;
    }

    public void setInnerTen(float innerTen) {
        this.innerTen = innerTen;
    }

    public float getOuterTen() {
        return outerTen;
    }

    public void setOuterTen(float outerTen) {
        this.outerTen = outerTen;
    }

    public void setScore(float score) {
        this.score = score;
        this.edited = true;
    }

    public String getCompetitionMembersListEntityUUID() {
        return competitionMembersListEntityUUID;
    }

    public void setCompetitionMembersListEntityUUID(String competitionMembersListEntityUUID) {
        this.competitionMembersListEntityUUID = competitionMembersListEntityUUID;
    }

    public MemberEntity getMember() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public OtherPersonEntity getOtherPersonEntity() {
        return otherPersonEntity;
    }

    public void setOtherPersonEntity(OtherPersonEntity otherPersonEntity) {
        this.otherPersonEntity = otherPersonEntity;
    }

    public boolean isAmmunition() {
        return ammunition;
    }

    public void toggleAmmunition() {
        this.ammunition = !this.ammunition;
    }

    public boolean isGun() {
        return gun;
    }

    public void toggleGun() {
        this.gun = !this.gun;
    }

    public void toggleDnf() {
        this.dnf = !this.dnf;
        this.edited = true;
    }

    public void toggleDsq() {
        this.dsq = !this.dsq;
        this.edited = true;
    }

    public void togglePk() {
        this.pk = !this.pk;
        this.edited = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMetricNumber() {
        return metricNumber;
    }

    public void setMetricNumber(int metricNumber) {
        this.metricNumber = metricNumber;
    }

    public float getHf() {
        return hf;
    }

    public void setHf(float hf) {
        this.hf = hf;
    }

    public int getProcedures() {
        return procedures;
    }

    public void setProcedures(int procedures) {
        this.procedures = procedures;
    }

    public float getAlfa() {
        return alfa;
    }

    public void setAlfa(float alfa) {
        this.alfa = alfa;
    }

    public float getCharlie() {
        return charlie;
    }

    public void setCharlie(float charlie) {
        this.charlie = charlie;
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public float getMiss() {
        return miss;
    }

    public void setMiss(float miss) {
        this.miss = miss;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
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
