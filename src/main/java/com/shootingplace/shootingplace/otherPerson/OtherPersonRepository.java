package com.shootingplace.shootingplace.otherPerson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtherPersonRepository extends JpaRepository<OtherPersonEntity, Integer> {

    List<OtherPersonEntity> findAllByActiveTrue();

    List<OtherPersonEntity> findAllByPhoneNumberAndActiveTrue(String phone);

    Optional<OtherPersonEntity> findByLicenseNumberAndActiveTrue(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    List<OtherPersonEntity> findAllByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    List<OtherPersonEntity> findAllByLicenseNumber(String s);
}
