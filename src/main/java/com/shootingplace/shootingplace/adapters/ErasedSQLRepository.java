package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.member.ErasedEntity;
import com.shootingplace.shootingplace.member.ErasedRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErasedSQLRepository extends ErasedRepository, JpaRepository<ErasedEntity, String> {
}
