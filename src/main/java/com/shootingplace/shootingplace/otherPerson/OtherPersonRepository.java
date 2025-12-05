package com.shootingplace.shootingplace.otherPerson;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OtherPersonRepository{
    Optional<OtherPersonEntity> findById(int otherID);

    List<OtherPersonEntity> findAll();
    @Query(nativeQuery = true, value = "SELECT * from shootingplace.other_person_entity where active")
    List<OtherPersonEntity> findAllByActiveTrue();
    @Query(nativeQuery = true, value = "SELECT * from shootingplace.other_person_entity where active = false")
    List<OtherPersonEntity> findAllByActiveFalse();

    OtherPersonEntity save(OtherPersonEntity entity);

    OtherPersonEntity getOne(Integer id);
    Optional<OtherPersonEntity> findByPhoneNumber(String phone);
    List<OtherPersonEntity> findAllByPhoneNumberAndActiveTrue(String phone);

    boolean existsById(int id);
}
