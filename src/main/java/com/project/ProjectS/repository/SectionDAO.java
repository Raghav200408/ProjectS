package com.project.ProjectS.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.project.ProjectS.model.SectionDTO;

@Repository
public interface SectionDAO extends CrudRepository<SectionDTO, Long> {

}