package com.shootingplace.shootingplace.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilesRepository {
    List<FilesEntity> findAll();

    FilesEntity save(FilesEntity filesEntity);

    boolean existsById(String uuid);

    void deleteById(String uuid);

    Optional<FilesEntity> findById(String uuid);

    Page<FilesEntity> findAll(Pageable page);

    @Query(nativeQuery = true, value = "Select uuid, belong_to_memberuuid, name, type, date, time, size, version from shootingplace.files_entity")
    Page<IFile> findAllByDateIsNotNullAndTimeIsNotNull(Pageable page);
    @Query(nativeQuery = true, value="Select ceiling(count(uuid)/50) from shootingplace.files_entity")
    int countAllRecordsDividedBy50();

    List<FilesEntity> findAllByDateIsNullAndTimeIsNull();

    @Query(nativeQuery = true, value = "SELECT uuid, belong_to_memberuuid, name, type, date, time, size, version FROM shootingplace.files_entity where belong_to_memberuuid like (:uuid)")
    List<IFile> findAllByBelongToMemberUUIDEquals(@Param("uuid") String uuid);

    @Query(nativeQuery = true, value = "SELECT uuid, belong_to_memberuuid, name, type, date, time, size, version FROM shootingplace.files_entity where name like '%raport_pracy%' and name like (:monthName) and name like (:year)")
    List<IFile> findAllByNameContains(@Param("monthName") String month, @Param("year") String year);

    FilesEntity getOne(String uuid);
}

