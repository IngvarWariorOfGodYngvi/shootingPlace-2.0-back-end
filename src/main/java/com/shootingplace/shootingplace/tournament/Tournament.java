package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tournament {

    private String uuid;
    private String name;
    private LocalDate date;
    private MemberDTO commissionRTSArbiter;
    private MemberDTO mainArbiter;

    private List<MemberDTO> arbitersList;
    @ManyToMany
    private List<MemberDTO> arbitersRTSList;

    @ManyToOne
    private OtherPersonEntity otherMainArbiter;
    @ManyToOne
    private OtherPersonEntity otherCommissionRTSArbiter;
    @ManyToMany
    private List<OtherPersonEntity> otherArbitersList;
    @ManyToMany
    private List<OtherPersonEntity> otherArbitersRTSList;
    @OneToMany(orphanRemoval = true)
    @OrderBy("name ASC")
    private List<CompetitionMembersList> competitionsList;
    private boolean open;
    private boolean wzss;
    private boolean ranking;
    private boolean dynamic;

    public boolean isWzss() {
        return wzss;
    }

    public void setWzss(boolean wzss) {
        this.wzss = wzss;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MemberDTO getCommissionRTSArbiter() {
        return commissionRTSArbiter;
    }

    public void setCommissionRTSArbiter(MemberDTO commissionRTSArbiter) {
        this.commissionRTSArbiter = commissionRTSArbiter;
    }

    public MemberDTO getMainArbiter() {
        return mainArbiter;
    }

    public void setMainArbiter(MemberDTO mainArbiter) {
        this.mainArbiter = mainArbiter;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public List<MemberDTO> getArbitersList() {
        return arbitersList;
    }

    public void setArbitersList(List<MemberDTO> arbitersList) {
        this.arbitersList = arbitersList;
    }

    public List<MemberDTO> getArbitersRTSList() {
        return arbitersRTSList;
    }

    public void setArbitersRTSList(List<MemberDTO> arbitersRTSList) {
        this.arbitersRTSList = arbitersRTSList;
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

    public List<OtherPersonEntity> getOtherArbitersRTSList() {
        return otherArbitersRTSList;
    }

    public void setOtherArbitersRTSList(List<OtherPersonEntity> otherArbitersRTSList) {
        this.otherArbitersRTSList = otherArbitersRTSList;
    }

    public List<CompetitionMembersList> getCompetitionsList() {
        return competitionsList;
    }

    public void setCompetitionsList(List<CompetitionMembersList> competitionsList) {
        this.competitionsList = competitionsList;
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
