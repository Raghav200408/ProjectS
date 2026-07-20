package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableHeaderRepository extends JpaRepository<TableHeader, Long> {

    boolean existsByName(String name);
    Optional<TableHeader> findByName(String name);


}