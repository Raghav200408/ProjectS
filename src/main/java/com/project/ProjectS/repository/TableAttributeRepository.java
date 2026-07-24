package com.project.ProjectS.repository;

import com.project.ProjectS.entity.TableAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableAttributeRepository extends JpaRepository<TableAttribute, Long> {

    boolean existsByName(String name);

    Optional<TableAttribute> findByName(String name);

}
