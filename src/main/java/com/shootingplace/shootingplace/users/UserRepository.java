package com.shootingplace.shootingplace.users;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<UserEntity> findAll();

    UserEntity save(UserEntity entity);

    Optional<UserEntity> findById(String uuid);

    boolean existsById(String uuid);

    UserEntity getOne(String uuid);

    UserEntity findByPinCode(String pinCode);

    boolean existsByPinCode(String pinCode);

    boolean existsBySecondName(String secondName);

    UserEntity findByMemberUuid (String memberUUID);

    void delete(UserEntity entity);
}
