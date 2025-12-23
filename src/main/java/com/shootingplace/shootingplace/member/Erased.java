package com.shootingplace.shootingplace.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Erased {

    private String uuid;

    private LocalDate date;
    private String erasedType;
    private String additionalDescription;

}
