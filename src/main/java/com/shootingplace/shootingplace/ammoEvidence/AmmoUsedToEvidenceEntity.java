package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedToEvidenceEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String caliberName;

    private String caliberUUID;
    @ManyToOne
    private MemberEntity memberEntity;

    private Integer counter;
    @ManyToOne
    private OtherPersonEntity otherPersonEntity;

    private String name;

    private LocalDate date;
    private LocalTime time;

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setMemberEntity(MemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public void setOtherPersonEntity(OtherPersonEntity otherPersonEntity) {
        this.otherPersonEntity = otherPersonEntity;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setName(String name) {
        this.name = name;
    }
}
