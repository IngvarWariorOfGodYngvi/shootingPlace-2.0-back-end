package com.shootingplace.shootingplace.score;

import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    private String uuid;

    private float score;

    private float innerTen;
    private float outerTen;
    private float hf;
    private int procedures;
    private float miss;


    private float alfa;
    private float charlie;
    private float delta;
    private List<Float> series;

    private String name;

    private int metricNumber;

    private boolean ammunition;
    private boolean gun;

    private boolean dnf;
    private boolean dsq;
    private boolean pk;
    private boolean edited;


    private String competitionMembersListEntityUUID;
    @OneToOne(orphanRemoval = true)
    private MemberDTO member;
    @OneToOne(orphanRemoval = true)
    private OtherPersonEntity otherPersonEntity;

}
