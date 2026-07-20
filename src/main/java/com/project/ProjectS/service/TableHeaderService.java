package com.project.ProjectS.service;

import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.model.TableHeaderRequestDTO;
import com.project.ProjectS.repository.TableHeaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class TableHeaderService {

    @Autowired
    private TableHeaderRepository repository;

    public String create(TableHeaderRequestDTO request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Table Name already exists");
        }

        TableHeader entity = new TableHeader();
        entity.setName(request.getName());

        repository.save(entity);

        return "Table Header created successfully";
    }

    public List<TableHeader> getAll() {

        return repository.findAll();
    }

    public TableHeader getById(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Header not found"));
    }

    public String update(Long id, TableHeaderRequestDTO request) {

        TableHeader entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Header not found"));

        entity.setName(request.getName());

        repository.save(entity);

        return "Table Header updated successfully";
    }

    public String delete(Long id) {

        TableHeader entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Header not found"));

        repository.delete(entity);

        return "Table Header deleted successfully";
    }


}