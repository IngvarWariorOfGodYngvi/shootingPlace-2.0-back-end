package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedEvidence {
    private String caliberName;

    private String userName;

    private MemberEntity memberEntity;

    private OtherPersonEntity otherPersonEntity;

    private Integer counter;

    private String caliberUUID;

    private LocalDate date;

    private LocalTime time;

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setMemberEntity(MemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setOtherPersonEntity(OtherPersonEntity otherPersonEntity) {
        this.otherPersonEntity = otherPersonEntity;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
