package com.shootingplace.shootingplace.barCodeCards;

import java.util.List;

public interface BarCodeCardRepository {
    BarCodeCardEntity save(BarCodeCardEntity entity);

    boolean existsByBarCode(String barCode);

    BarCodeCardEntity findByBarCode(String number);

    List<BarCodeCardEntity> findAllByBelongsTo(String uuid);

    List<BarCodeCardEntity> findAll();

//    boolean existsByBelongsTo(String belongsTo);
}
