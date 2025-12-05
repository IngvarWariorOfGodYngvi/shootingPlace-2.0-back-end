package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private String name;
    private LocalDate date;

    @ManyToOne
    private MemberEntity mainArbiter;
    @ManyToOne
    private MemberEntity commissionRTSArbiter;
    @ManyToMany
    private List<MemberEntity> arbitersList;
    @ManyToMany
    private List<MemberEntity> arbitersRTSList;

    @ManyToOne
    private OtherPersonEntity otherMainArbiter;
    @ManyToOne
    private OtherPersonEntity otherCommissionRTSArbiter;
    @ManyToMany
    private List<OtherPersonEntity> otherArbitersList;
    @ManyToMany
    private List<OtherPersonEntity> otherArbitersRTSList;


    @OneToMany(orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<CompetitionMembersListEntity> competitionsList;
    private boolean open;
    private boolean WZSS;
    private boolean ranking;
    private boolean dynamic;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MemberEntity getCommissionRTSArbiter() {
        return commissionRTSArbiter;
    }

    public void setCommissionRTSArbiter(MemberEntity commissionRTSArbiter) {
        this.commissionRTSArbiter = commissionRTSArbiter;
    }

    public MemberEntity getMainArbiter() {
        return mainArbiter;
    }

    public void setMainArbiter(MemberEntity mainArbiter) {
        this.mainArbiter = mainArbiter;
    }

    public List<MemberEntity> getArbitersList() {
        return arbitersList;
    }

    public void setArbitersList(List<MemberEntity> arbitersList) {
        this.arbitersList = arbitersList;
    }

    public List<CompetitionMembersListEntity> getCompetitionsList() {
        return competitionsList;
    }

    public void setCompetitionsList(List<CompetitionMembersListEntity> competitionsList) {
        this.competitionsList = competitionsList;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public OtherPersonEntity getOtherMainArbiter() {
        return otherMainArbiter;
    }

    public void setOtherMainArbiter(OtherPersonEntity otherMainArbiter) {
        this.otherMainArbiter = otherMainArbiter;
    }

    public OtherPersonEntity getOtherCommissionRTSArbiter() {
        return otherCommissionRTSArbiter;
    }

    public void setOtherCommissionRTSArbiter(OtherPersonEntity otherCommissionRTSArbiter) {
        this.otherCommissionRTSArbiter = otherCommissionRTSArbiter;
    }

    public List<OtherPersonEntity> getOtherArbitersList() {
        return otherArbitersList;
    }

    public void setOtherArbitersList(List<OtherPersonEntity> otherArbitersList) {
        this.otherArbitersList = otherArbitersList;
    }

    public List<MemberEntity> getArbitersRTSList() {
        return arbitersRTSList;
    }

    public void setArbitersRTSList(List<MemberEntity> arbitersRTSList) {
        this.arbitersRTSList = arbitersRTSList;
    }

    public List<OtherPersonEntity> getOtherArbitersRTSList() {
        return otherArbitersRTSList;
    }

    public void setOtherArbitersRTSList(List<OtherPersonEntity> otherArbitersRTSList) {
        this.otherArbitersRTSList = otherArbitersRTSList;
    }

    public boolean isWZSS() {
        return WZSS;
    }

    public void setWZSS(boolean WZSS) {
        this.WZSS = WZSS;
    }

    public boolean isRanking() {
        return ranking;
    }

    public void setRanking(boolean ranking) {
        this.ranking = ranking;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
