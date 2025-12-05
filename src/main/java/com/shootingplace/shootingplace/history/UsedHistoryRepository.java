package com.shootingplace.shootingplace.history;

import java.util.List;

public interface UsedHistoryRepository{
    List<UsedHistoryEntity> findAll();

    List<UsedHistoryEntity> findAllByEvidenceUUID(String evidenceUUID);
    List<UsedHistoryEntity> findAllByUsedType(String type);
    UsedHistoryEntity findByGunUUIDAndReturnToStoreFalse(String gunUUID);
    List<UsedHistoryEntity> findAllByUsedTypeAndReturnToStoreFalse(String type);

    UsedHistoryEntity save(UsedHistoryEntity entity);
}
