package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableAttributeRepository extends JpaRepository<TableAttribute, Long> {

    boolean existsByName(String name);

}