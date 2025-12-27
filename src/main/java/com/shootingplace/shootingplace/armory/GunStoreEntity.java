package com.shootingplace.shootingplace.armory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Getter
@Setter
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

}
