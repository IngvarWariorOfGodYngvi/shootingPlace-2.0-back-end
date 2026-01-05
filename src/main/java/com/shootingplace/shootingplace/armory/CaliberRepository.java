package com.shootingplace.shootingplace.armory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CaliberRepository extends JpaRepository<CaliberEntity, String> {
CaliberEntity findCaliberByName(String name);
}
