package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
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

}
