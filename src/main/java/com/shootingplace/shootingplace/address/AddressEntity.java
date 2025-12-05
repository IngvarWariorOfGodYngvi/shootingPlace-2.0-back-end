package com.shootingplace.shootingplace.address;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    @Pattern(regexp = "\\d{2}-\\d{3}")
    private String zipCode;
    private String postOfficeCity;
    private String street;
    private String streetNumber;
    private String flatNumber;

    public String getUuid() {
        return uuid;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPostOfficeCity() {
        return postOfficeCity;
    }

    public void setPostOfficeCity(String postOfficeCity) {
        this.postOfficeCity = postOfficeCity;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getFlatNumber() {
        return flatNumber;
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
        String zipCode = this.zipCode != null ? this.zipCode : "";
        String postOfficeCity = this.postOfficeCity != null ? this.postOfficeCity : "";
        String street = this.street != null ? this.street : "";
        String streetNumber = this.streetNumber != null ? this.streetNumber : "";
        String flatNumber = this.flatNumber != null ? "m." + this.flatNumber : "";
        return zipCode + " " + postOfficeCity + " " + street + " " + streetNumber + " " + flatNumber;
    }
}
