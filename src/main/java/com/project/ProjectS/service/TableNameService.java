package com.project.ProjectS.service;

import com.project.ProjectS.model.TableNameRequestDTO;
import com.project.ProjectS.model.TableNameResponseDTO;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.repository.TableNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableNameService {

    @Autowired
    private TableNameRepository repository;

    public String create(TableNameRequestDTO request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Table Name already exists");
        }

        TableName entity = new TableName();
        entity.setName(request.getName());

        repository.save(entity);

        return "Table Name created successfully";
    }

    public List<TableName> getAll() {

        return repository.findAll();
    }

    public TableName getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Name not found"));
    }

    public String update(Long id, TableNameRequestDTO request) {

        TableName entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Name not found"));

        entity.setName(request.getName());

        repository.save(entity);

        return "Table Name updated successfully";
    }

    public String delete(Long id) {

        TableName entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Name not found"));

        repository.delete(entity);

        return "Table Name deleted successfully";
    }


}