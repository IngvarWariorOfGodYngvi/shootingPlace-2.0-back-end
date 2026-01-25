package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.tournament.axis.ShootingAxisEntity;
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
    private String location;

    @ManyToOne
    private MemberEntity mainArbiter;

    @ManyToOne
    private MemberEntity commissionRTSArbiter;

    @ManyToOne
    private OtherPersonEntity otherMainArbiter;

    @ManyToOne
    private OtherPersonEntity otherCommissionRTSArbiter;

    @ManyToMany
    private List<MemberEntity> arbitersRTSList;

    @ManyToMany
    private List<OtherPersonEntity> otherArbitersRTSList;

    @ManyToMany
    private List<MemberEntity> technicalSupportList;

    @ManyToMany
    private List<OtherPersonEntity> otherTechnicalSupportList;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<ShootingAxisEntity> shootingAxis;

    @OneToMany(orphanRemoval = true)
    @OrderBy("ordering ASC")
    private List<CompetitionMembersListEntity> competitionsList;

    private boolean open;
    private boolean WZSS;
}
