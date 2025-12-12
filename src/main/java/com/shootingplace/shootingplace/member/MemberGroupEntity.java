package com.shootingplace.shootingplace.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "member_group_entity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    private Boolean active;
}
