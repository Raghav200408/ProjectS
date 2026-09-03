package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableNameRepository extends JpaRepository<TableName, Long> {

    boolean existsByName(String name);

    Optional<TableName> findByName(String name);

    Optional<TableName> findByNameIgnoreCase(String name);

    @Query(value = """
            SELECT *
            FROM table_names t
            WHERE LOWER(TRIM(REGEXP_REPLACE(t.name, '[[:space:]]+', ' ', 'g')))
                  = LOWER(TRIM(REGEXP_REPLACE(:name, '[[:space:]]+', ' ', 'g')))
            LIMIT 1
            """, nativeQuery = true)
    Optional<TableName> findByNormalizedName(@Param("name") String name);

}
