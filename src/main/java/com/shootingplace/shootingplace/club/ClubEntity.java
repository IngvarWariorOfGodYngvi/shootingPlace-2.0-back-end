package com.shootingplace.shootingplace.club;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubEntity {

    @Id
    private Integer id;
    private String shortName;
    private String fullName;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private String wzss;
    private String vovoidership; // tak jest w SOZ
    private String city;
    private String street;
    private String houseNumber;
    private String appartmentNumber; // tak jest w SOZ
    private String url;

}
