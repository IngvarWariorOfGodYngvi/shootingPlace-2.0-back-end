package com.shootingplace.shootingplace.club;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Club {
    private Integer id;
    private String shortName;
    private String fullName;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private String wzss;
    private String vovoidership;
    private String city;
    private String street;
    private String houseNumber;
    private String appartmentNumber;
    private String url;

}
