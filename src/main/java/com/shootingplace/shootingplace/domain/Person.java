package com.shootingplace.shootingplace.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    private String uuid;

    private String secondName;

    private String firstName;

    public String getFullName() {
        return this.secondName + ' ' + this.firstName;
    }
}
