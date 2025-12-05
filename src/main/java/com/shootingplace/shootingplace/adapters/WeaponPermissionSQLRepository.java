package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionEntity;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface WeaponPermissionSQLRepository extends WeaponPermissionRepository, JpaRepository<WeaponPermissionEntity,String> {
}
