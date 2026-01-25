package com.shootingplace.shootingplace.soz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class SozConfig {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

}

