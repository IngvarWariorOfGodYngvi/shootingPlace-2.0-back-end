package com.shootingplace.shootingplace.address;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressEntity {

    @Id
    @UuidGenerator
    private String uuid;

    @Pattern(regexp = "\\d{2}-\\d{3}")
    private String zipCode;
    private String postOfficeCity;
    private String street;
    private String streetNumber;
    private String flatNumber;

    @Override
    public String toString() {
        String flatNumber = this.flatNumber != null ? "m." + this.flatNumber : "";
        return postOfficeCity + " " + zipCode + " " + street + " " + streetNumber + " " + flatNumber;
    }

    public String fullAddress() {
        String zipCode = !this.zipCode.isEmpty() ? this.zipCode : "";
        String postOfficeCity = !this.postOfficeCity.isEmpty() ? this.postOfficeCity : "";
        String street = !this.street.isEmpty() ? this.street : "";
        String streetNumber = !this.streetNumber.isEmpty() ? this.streetNumber : "";
        String flatNumber = !this.flatNumber.isEmpty() ? "m." + this.flatNumber : "";
        return zipCode + " " + postOfficeCity + " " + street + " " + streetNumber + " " + flatNumber;
    }
}
