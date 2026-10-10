package com.project.ProjectS.service;

import com.project.ProjectS.model.TableNameRequestDTO;
import com.project.ProjectS.model.TableNameResponseDTO;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.repository.TableNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableNameService {
    private static final Logger log = LogManager.getLogger(TableNameService.class);
    @Autowired
    public TableNameService(TableNameRepository repository) {
        this.repository = repository;
    }

    private final TableNameRepository repository;

    public String create(TableNameRequestDTO request) {
        log.info("Creating table name: name={} activeRow={}", request.getName(), request.getActiveRow());

        if (repository.existsByName(request.getName())) {
            log.warn("Duplicate table name creation blocked: name={}", request.getName());
            throw new RuntimeException("Table Name already exists");
        }

        TableName entity = new TableName();
        entity.setName(request.getName());
        if (request.getActiveRow() != null) {
            entity.setActiveRow(request.getActiveRow());
        }

        repository.save(entity);
        log.info("Table name created successfully: name={}", request.getName());

        return "Table Name created successfully";
    }

    public List<TableName> getAll() {
        List<TableName> result = repository.findAll();
        log.debug("Fetched all table names: count={}", result.size());
        return result;
    }

    public TableName getById(Long id) {
        log.debug("Fetching table name by id={}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Table name not found: id={}", id);
                    return new RuntimeException("Table Name not found");
                });
    }

    public String update(Long id, TableNameRequestDTO request) {
        log.info("Updating table name: id={} name={} activeRow={}", id, request.getName(), request.getActiveRow());

        TableName entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Could not update table name; id not found: id={}", id);
                    return new RuntimeException("Table Name not found");
                });

        entity.setName(request.getName());
        if (request.getActiveRow() != null) {
            entity.setActiveRow(request.getActiveRow());
        }

        repository.save(entity);
        log.info("Table name updated successfully: id={}", id);

        return "Table Name updated successfully";
    }

    public String delete(Long id) {
        log.info("Deleting table name: id={}", id);

        TableName entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Could not delete table name; id not found: id={}", id);
                    return new RuntimeException("Table Name not found");
                });

        repository.delete(entity);
        log.info("Table name deleted successfully: id={}", id);

        return "Table Name deleted successfully";
    }


}
