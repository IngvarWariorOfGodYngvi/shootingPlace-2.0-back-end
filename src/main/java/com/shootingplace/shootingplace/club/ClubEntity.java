package com.shootingplace.shootingplace.club;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public void setShortName(String name) {
        this.shortName = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setWzss(String wzss) {
        this.wzss = wzss;
    }

    public void setVovoidership(String voivodeship) {
        this.vovoidership = voivodeship;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setAppartmentNumber(String appartmentNumber) {
        this.appartmentNumber = appartmentNumber;

    }

    @Override
    public String toString() {
        return "ClubEntity{" +
                "shortName='" + shortName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", wzss='" + wzss + '\'' +
                ", vovoidership='" + vovoidership + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", houseNumber='" + houseNumber + '\'' +
                ", appartmentNumber='" + appartmentNumber + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
