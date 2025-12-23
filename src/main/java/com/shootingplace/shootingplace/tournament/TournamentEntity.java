package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentEntity {

    @Id
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCommissionRTSArbiter(MemberEntity commissionRTSArbiter) {
        this.commissionRTSArbiter = commissionRTSArbiter;
    }

    public void setMainArbiter(MemberEntity mainArbiter) {
        this.mainArbiter = mainArbiter;
    }

    public void setArbitersList(List<MemberEntity> arbitersList) {
        this.arbitersList = arbitersList;
    }

    public void setCompetitionsList(List<CompetitionMembersListEntity> competitionsList) {
        this.competitionsList = competitionsList;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setOtherMainArbiter(OtherPersonEntity otherMainArbiter) {
        this.otherMainArbiter = otherMainArbiter;
    }

    public void setOtherCommissionRTSArbiter(OtherPersonEntity otherCommissionRTSArbiter) {
        this.otherCommissionRTSArbiter = otherCommissionRTSArbiter;
    }

    public void setOtherArbitersList(List<OtherPersonEntity> otherArbitersList) {
        this.otherArbitersList = otherArbitersList;
    }

    public void setArbitersRTSList(List<MemberEntity> arbitersRTSList) {
        this.arbitersRTSList = arbitersRTSList;
    }

    public void setOtherArbitersRTSList(List<OtherPersonEntity> otherArbitersRTSList) {
        this.otherArbitersRTSList = otherArbitersRTSList;
    }

    public void setWZSS(boolean WZSS) {
        this.WZSS = WZSS;
    }

    public void setRanking(boolean ranking) {
        this.ranking = ranking;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
