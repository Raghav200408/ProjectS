package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableAttributeRepository extends JpaRepository<TableAttribute, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndTableHeader(String name, TableHeader tableHeader);

    Optional<TableAttribute> findByName(String name);

    Optional<TableAttribute> findByNameAndTableHeader(String name, TableHeader tableHeader);

    List<TableAttribute> findByRowStatusIgnoreCase(String rowStatus);
}
