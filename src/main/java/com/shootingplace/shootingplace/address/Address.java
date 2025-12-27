package com.shootingplace.shootingplace.address;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String zipCode;
    private String postOfficeCity;
    private String street;
    private String streetNumber;
    private String flatNumber;

}
