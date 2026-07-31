package com.project.ProjectS.repository;

import com.project.ProjectS.entity.RuleEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface RuleEngineRepository extends JpaRepository<RuleEngine, Long> {


    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM RuleEngine r
        WHERE r.chapter.chapterId = :chapterId
        AND LOWER(TRIM(COALESCE(r.fieldName.name,''))) 
                = LOWER(TRIM(:fieldName))
        AND LOWER(TRIM(COALESCE(r.fieldType.name,''))) 
                = LOWER(TRIM(:fieldType))
        AND LOWER(TRIM(COALESCE(r.relationshipName,''))) 
                = LOWER(TRIM(:relationshipName))
    """)
    boolean existsRule(

            @Param("chapterId")
            Long chapterId,

            @Param("fieldName")
            String fieldName,

            @Param("fieldType")
            String fieldType,

            @Param("relationshipName")
            String relationshipName
    );

}