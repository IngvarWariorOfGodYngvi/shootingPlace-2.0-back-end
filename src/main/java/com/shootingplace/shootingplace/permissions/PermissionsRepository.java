package com.shootingplace.shootingplace.permissions;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionsRepository extends JpaRepository<MemberPermissionsEntity, String> {
}
