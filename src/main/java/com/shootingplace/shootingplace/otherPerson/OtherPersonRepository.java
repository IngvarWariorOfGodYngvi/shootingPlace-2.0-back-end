package com.shootingplace.shootingplace.otherPerson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtherPersonRepository extends JpaRepository<OtherPersonEntity, Integer> {

    List<OtherPersonEntity> findAllByActiveTrue();

    List<OtherPersonEntity> findAllByPhoneNumberAndActiveTrue(String phone);
}
