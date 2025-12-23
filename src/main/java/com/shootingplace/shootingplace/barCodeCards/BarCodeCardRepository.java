package com.shootingplace.shootingplace.barCodeCards;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarCodeCardRepository extends JpaRepository<BarCodeCardEntity,String> {

    boolean existsByBarCode(String barCode);

    BarCodeCardEntity findByBarCode(String number);

    List<BarCodeCardEntity> findAllByBelongsTo(String uuid);
}
