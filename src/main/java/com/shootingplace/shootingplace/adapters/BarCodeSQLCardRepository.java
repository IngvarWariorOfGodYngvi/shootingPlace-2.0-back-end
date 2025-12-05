package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface BarCodeSQLCardRepository extends BarCodeCardRepository, JpaRepository<BarCodeCardEntity,String> {
}
