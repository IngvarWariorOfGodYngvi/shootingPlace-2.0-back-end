package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
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

}
