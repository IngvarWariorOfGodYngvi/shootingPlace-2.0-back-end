package com.shootingplace.shootingplace.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberGroupRepository extends JpaRepository<MemberGroupEntity, Long> {

    Optional<MemberGroupEntity> findByName(String name);

    boolean existsByName(String name);
}
