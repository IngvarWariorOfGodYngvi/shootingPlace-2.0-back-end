package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.history.ChangeHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity extends Person {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;
    @OneToOne
    private MemberEntity member;

    private String firstName;

    private String secondName;
    private Integer otherID;

    private String pinCode;
    @OneToMany
    private List<BarCodeCardEntity> barCodeCardList;

    private boolean active;
    @OrderBy("dayNow DESC, timeNow DESC")
    @OneToMany(orphanRemoval = true)
    private List<ChangeHistoryEntity> changeHistoryEntities;

    private String userPermissionsList;

    public List<String> getUserPermissionsList() {
        List<String> vals = new ArrayList<>();
        if (userPermissionsList != null) {
            for (String s : userPermissionsList.split(";")) {
                vals.add(String.valueOf(s));
            }
        }
        return vals;
    }

    public void setUserPermissionsList(List<String> userPermissionsList) {
        String value = "";
        for (String f : userPermissionsList) {
            value = value.concat(f + ";");
        }
        this.userPermissionsList = value;
    }


    @Nullable
    public MemberEntity getMember() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<BarCodeCardEntity> getBarCodeCardList() {
        return barCodeCardList;
    }

    public void setBarCodeCardList(List<BarCodeCardEntity> barCodeCardList) {
        this.barCodeCardList = barCodeCardList;
    }

    public List<ChangeHistoryEntity> getList() {
        return changeHistoryEntities;
    }

    public void setList(List<ChangeHistoryEntity> changeHistoryEntities) {
        this.changeHistoryEntities = changeHistoryEntities;
    }

    public Integer getOtherID() {
        return otherID;
    }

    public void setOtherID(Integer otherID) {
        this.otherID = otherID;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "uuid='" + uuid + '\'' +
                ", member=" + member +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", otherID=" + otherID +
                ", pinCode='" + pinCode + '\'' +
                ", barCodeCardList=" + barCodeCardList +
                ", active=" + active +
                ", changeHistoryEntities=" + changeHistoryEntities +
                ", userPermissionsList='" + userPermissionsList + '\'' +
                '}';
    }

    public String getFullName() {
        return this.secondName + ' ' + this.firstName;
    }
}
