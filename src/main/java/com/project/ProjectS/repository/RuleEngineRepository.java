package com.project.ProjectS.repository;

import com.project.ProjectS.entity.RuleEngine;
import com.project.ProjectS.entity.TableAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuleEngineRepository extends JpaRepository<RuleEngine, Long> {

}