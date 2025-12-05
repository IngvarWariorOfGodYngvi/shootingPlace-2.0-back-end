package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.address.AddressRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface AddressSQLRepository extends AddressRepository, JpaRepository<AddressEntity,String> {
}
