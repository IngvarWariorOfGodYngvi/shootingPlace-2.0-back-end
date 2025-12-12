package com.shootingplace.shootingplace.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilesRepository extends JpaRepository<FilesEntity, String> {

    @Query(
            nativeQuery = true,
            value = """
            SELECT uuid, belong_to_memberuuid, name, type, date, time, size, version
            FROM shootingplace.files_entity
        """
    )
    Page<IFile> findAllByDateIsNotNullAndTimeIsNotNull(Pageable page);

    @Query(
            nativeQuery = true,
            value = "SELECT CEILING(COUNT(uuid) / 50.0) FROM shootingplace.files_entity"
    )
    int countAllRecordsDividedBy50();

    @Query(
            nativeQuery = true,
            value = """
            SELECT uuid, belong_to_memberuuid, name, type, date, time, size, version
            FROM shootingplace.files_entity
            WHERE belong_to_memberuuid = :uuid
        """
    )
    List<IFile> findAllByBelongToMemberUUIDEquals(@Param("uuid") String uuid);

    @Query(
            nativeQuery = true,
            value = """
            SELECT uuid, belong_to_memberuuid, name, type, date, time, size, version
            FROM shootingplace.files_entity
            WHERE name LIKE '%raport_pracy%'
              AND name LIKE :monthName
              AND name LIKE :year
        """
    )
    List<IFile> findAllByNameContains(
            @Param("monthName") String month,
            @Param("year") String year
    );
}
