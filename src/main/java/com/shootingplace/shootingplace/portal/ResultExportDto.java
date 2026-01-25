package com.shootingplace.shootingplace.portal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Builder
@Getter
@Setter
public class ResultExportDto {

    private String id;

    private String license;
    private String firstName;
    private String lastName;
    private String club;
    private String score;
    private Map<String, String> extra;
    private String details;
    private Integer place;
}

