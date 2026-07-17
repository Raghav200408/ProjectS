package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableNameRepository extends JpaRepository<TableName, Long> {

    boolean existsByName(String name);

    Optional<TableName> findByName(String name);

}