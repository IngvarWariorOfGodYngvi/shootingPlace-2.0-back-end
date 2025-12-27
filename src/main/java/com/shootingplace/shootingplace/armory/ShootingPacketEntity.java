package com.shootingplace.shootingplace.armory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShootingPacketEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String name;
    @OneToMany
    private List<CaliberForShootingPacketEntity> calibers;
    private float price;

}
