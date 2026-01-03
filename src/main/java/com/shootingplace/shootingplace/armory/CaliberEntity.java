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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaliberEntity {
    @Id
    @UuidGenerator
    private String uuid;

    private String name;
    private Integer quantity;
    private boolean active;
    @OneToMany
    private List<CaliberUsedEntity> ammoUsed;
    @OneToMany
    private List<CalibersAddedEntity> ammoAdded;

    private float unitPrice;
    private float unitPriceForNotMember;

    public void changeActive(){
        this.active = !this.active;
    }

}
