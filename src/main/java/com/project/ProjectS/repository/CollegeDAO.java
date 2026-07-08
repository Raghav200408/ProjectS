package com.project.ProjectS.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.model.CollegeDTO;

@Repository
public interface CollegeDAO extends CrudRepository<CollegeDTO, Long> {

}