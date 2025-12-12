package com.shootingplace.shootingplace.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByPinCode(String pinCode);

    boolean existsByPinCode(String pinCode);

    boolean existsBySecondName(String secondName);

    Optional<UserEntity> findByMemberUuid(String memberUUID);
}
