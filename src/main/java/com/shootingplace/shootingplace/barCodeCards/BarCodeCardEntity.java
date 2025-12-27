package com.shootingplace.shootingplace.barCodeCards;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BarCodeCardEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String barCode;

    private boolean isActive;
    /**
     * enter uuid of Member / User
     */
    private String belongsTo;
    /**
     * enter Member / User
     */
    private String type;

    private boolean isMaster;

    private int useCounter;

    private LocalDate activatedDay;

    public void addCountUse() {
        this.useCounter += 1;
    }
}
