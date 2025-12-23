package com.shootingplace.shootingplace.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Address {

    private String zipCode;
    private String postOfficeCity;
    private String street;
    private String streetNumber;
    private String flatNumber;

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setPostOfficeCity(String postOfficeCity) {
        this.postOfficeCity = postOfficeCity;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    @Override
    public String toString() {
        String flatNumber = this.flatNumber != null ? "m." + this.flatNumber : "";
        return postOfficeCity + " " + zipCode + " " + street + " " + streetNumber + " " + flatNumber;
    }

    public String fullAddress() {
        String flatNumber = this.flatNumber != null ? this.flatNumber : "";
        return street + " " + streetNumber + " " + flatNumber + " " + zipCode + " " + postOfficeCity;
    }

}
