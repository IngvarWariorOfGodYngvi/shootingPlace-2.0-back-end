package com.shootingplace.shootingplace.tournament.axis;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShootingAxisEntity {

    @Id
    @UuidGenerator
    private String uuid;

    @ManyToOne(optional = false)
    private TournamentEntity tournament;

    @Column(nullable = false)
    private Integer ordering;

    @Column(nullable = false)
    private String name;

    /**
     * Kierownik osi:
     * - NULL = sędzia główny turnieju
     */
    private String leaderUUID;
    private String leaderName;

    @Enumerated(EnumType.STRING)
    private AxisArbiterType leaderType;

    @ManyToMany
    private List<MemberEntity> axisArbiters = new ArrayList<>();

    @ManyToMany
    private List<OtherPersonEntity> otherAxisArbiters = new ArrayList<>();
}

