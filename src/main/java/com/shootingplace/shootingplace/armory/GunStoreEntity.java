package com.shootingplace.shootingplace.armory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunStoreEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;
    @NotNull
    private String typeName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("caliber ASC, modelName ASC")
    private List<GunEntity> gunEntityList;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("caliber ASC, modelName ASC")
    private List<GunEntity> removedGunEntityList;

    public String getUuid() {
        return uuid;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<GunEntity> getGunEntityList() {
        return gunEntityList;
    }

    public void setGunEntityList(List<GunEntity> gunEntityList) {
        this.gunEntityList = gunEntityList;
    }

    public List<GunEntity> getRemovedGunEntityList() {
        return removedGunEntityList;
    }

    public void setRemovedGunEntityList(List<GunEntity> removedGunEntityList) {
        this.removedGunEntityList = removedGunEntityList;
    }
}
