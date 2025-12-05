package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.users.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeHistoryEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;
    @OneToOne
    private UserEntity userEntity;
    private String classNamePlusMethod;
    private String belongsTo;
    private LocalDate dayNow;
    private String timeNow;

    public String getUuid() {
        return uuid;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public String getClassNamePlusMethod() {
        return classNamePlusMethod;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public LocalDate getDayNow() {
        return dayNow;
    }

    public String getTimeNow() {
        return timeNow;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public void setClassNamePlusMethod(String classNamePlusMethod) {
        this.classNamePlusMethod = classNamePlusMethod;
    }

    public void setBelongsTo(String belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void setDayNow(LocalDate dayNow) {
        this.dayNow = dayNow;
    }

    public void setTimeNow(String timeNow) {
        this.timeNow = timeNow;
    }
}
