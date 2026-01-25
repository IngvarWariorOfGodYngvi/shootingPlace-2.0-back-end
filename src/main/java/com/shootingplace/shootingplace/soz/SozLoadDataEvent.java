package com.shootingplace.shootingplace.soz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SozLoadDataEvent(
        boolean IsSuccess,
        List<String> Errors,
        Person Person
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Person(
            String Pesel,
            String FirstName,
            String LastName,
            String Email
    ) {}
}
