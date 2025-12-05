package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserSQLRepository extends UserRepository, JpaRepository<UserEntity,String> {
}
