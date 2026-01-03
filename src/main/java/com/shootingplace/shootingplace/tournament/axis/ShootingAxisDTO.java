package com.shootingplace.shootingplace.tournament.axis;

import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShootingAxisDTO {

    private String uuid;

    private String tournamentUUID;

    private Integer ordering;

    private String name;

    /**
     * Kierownik osi:
     * - NULL = sędzia główny turnieju
     */
    private String leaderName;

    @Enumerated(EnumType.STRING)
    private AxisArbiterType leaderType;
    private List<MemberDTO> axisArbiters;

    private List<OtherPersonEntity> otherAxisArbiters;

}
