package com.project.ProjectS.service;

import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.model.TableHeaderRequestDTO;
import com.project.ProjectS.repository.TableHeaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class TableHeaderService {
    private static final Logger log = LogManager.getLogger(TableHeaderService.class);
    @Autowired
    public TableHeaderService(TableHeaderRepository repository) {
        this.repository = repository;
    }

    private final TableHeaderRepository repository;

    public String create(TableHeaderRequestDTO request) {
        log.info("Creating table header: name={} activeRow={}", request.getName(), request.getActiveRow());

        if (repository.existsByName(request.getName())) {
            log.warn("Duplicate table header creation blocked: name={}", request.getName());
            throw new RuntimeException("Table Name already exists");
        }

        TableHeader entity = new TableHeader();
        entity.setName(request.getName());
        if (request.getActiveRow() != null) {
            entity.setActiveRow(request.getActiveRow());
        }

        repository.save(entity);
        log.info("Table header created successfully: name={}", request.getName());

        return "Table Header created successfully";
    }

    public List<TableHeader> getAll() {
        List<TableHeader> result = repository.findAll();
        log.debug("Fetched all table headers: count={}", result.size());
        return result;
    }

    public TableHeader getById(Long id) {
        log.debug("Fetching table header by id={}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Table header not found: id={}", id);
                    return new RuntimeException("Table Header not found");
                });
    }

    public String update(Long id, TableHeaderRequestDTO request) {
        log.info("Updating table header: id={} name={} activeRow={}", id, request.getName(), request.getActiveRow());

        TableHeader entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Could not update table header; id not found: id={}", id);
                    return new RuntimeException("Table Header not found");
                });

        entity.setName(request.getName());
        if (request.getActiveRow() != null) {
            entity.setActiveRow(request.getActiveRow());
        }

        repository.save(entity);
        log.info("Table header updated successfully: id={}", id);

        return "Table Header updated successfully";
    }

    public String delete(Long id) {
        log.info("Deleting table header: id={}", id);

        TableHeader entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Could not delete table header; id not found: id={}", id);
                    return new RuntimeException("Table Header not found");
                });

        repository.delete(entity);
        log.info("Table header deleted successfully: id={}", id);

        return "Table Header deleted successfully";
    }


}
