package com.shootingplace.shootingplace.club;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<ClubEntity, Integer> {
    boolean existsByShortName(String shortName);

    ClubEntity findByShortName(String shortName);

    @Query(nativeQuery = true, value = "select coalesce(max(c.id), 0) from shootingplace.club_entity c")
    Integer findMaxId();

    @Query(nativeQuery = true, value = """
    select count(*) > 0
    from shootingplace.club_entity c
    where lower(replace(c.short_name, ' ', '')) =
          lower(replace(:shortName, ' ', ''))
""")
    long countByNormalizedShortName(@Param("shortName") String shortName);

}
