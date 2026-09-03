package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TableHeaderRepository extends JpaRepository<TableHeader, Long> {

    boolean existsByName(String name);

    Optional<TableHeader> findByName(String name);

    Optional<TableHeader> findByNameIgnoreCase(String name);

    @Query(value = """
            SELECT *
            FROM table_headers h
            WHERE LOWER(TRIM(REGEXP_REPLACE(h.name, '[[:space:]]+', ' ', 'g')))
                  = LOWER(TRIM(REGEXP_REPLACE(:name, '[[:space:]]+', ' ', 'g')))
            LIMIT 1
            """, nativeQuery = true)
    Optional<TableHeader> findByNormalizedName(@Param("name") String name);

}
