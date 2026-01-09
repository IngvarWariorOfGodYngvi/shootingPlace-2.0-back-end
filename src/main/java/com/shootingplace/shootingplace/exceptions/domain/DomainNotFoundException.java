package com.shootingplace.shootingplace.exceptions.domain;

import lombok.Getter;

@Getter
public class DomainNotFoundException extends RuntimeException {

    private final String entity;
    private final String identifier;

    public DomainNotFoundException(String entity, String identifier) {
        this.entity = entity;
        this.identifier = identifier;
    }
}
