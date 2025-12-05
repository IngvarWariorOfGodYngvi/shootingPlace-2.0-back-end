package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.member.permissions.MemberPermissionsEntity;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPermissionSQLRepository extends MemberPermissionsRepository, JpaRepository<MemberPermissionsEntity,String> {
}
