package com.shootingplace.shootingplace.settings;

import com.shootingplace.shootingplace.address.AddressEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettingsEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AddressEntity WPAAddress;

    private String contributionTypeAdult;
    private String contributionTypeNonAdult;

}
