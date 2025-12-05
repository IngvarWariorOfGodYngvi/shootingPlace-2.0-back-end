package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtherPersonSQLRepository extends OtherPersonRepository, JpaRepository<OtherPersonEntity, Integer> {
}
