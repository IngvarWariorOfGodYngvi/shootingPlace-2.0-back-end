package com.shootingplace.shootingplace.armory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunStoreEntity {

    @Id
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

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setGunEntityList(List<GunEntity> gunEntityList) {
        this.gunEntityList = gunEntityList;
    }

    public void setRemovedGunEntityList(List<GunEntity> removedGunEntityList) {
        this.removedGunEntityList = removedGunEntityList;
    }
}
