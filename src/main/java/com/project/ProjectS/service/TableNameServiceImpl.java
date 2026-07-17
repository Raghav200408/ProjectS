package com.project.ProjectS.service;

import com.project.ProjectS.dto.TableNameRequestDTO;
import com.project.ProjectS.dto.TableNameResponseDTO;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.repository.TableNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableNameServiceImpl {

    @Autowired
    private TableNameRepository repository;

    public TableNameResponseDTO create(TableNameRequestDTO request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Table Name already exists");
        }

        TableName entity = new TableName();
        entity.setName(request.getName());

        entity = repository.save(entity);

        return convert(entity);
    }

    public List<TableNameResponseDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public TableNameResponseDTO getById(Long id) {

        TableName entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Name not found"));

        return convert(entity);
    }

    public TableNameResponseDTO update(Long id, TableNameRequestDTO request) {

        TableName entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Name not found"));

        entity.setName(request.getName());

        entity = repository.save(entity);

        return convert(entity);
    }

    public String delete(Long id) {

        TableName entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Name not found"));

        repository.delete(entity);

        return "Table Name deleted successfully";
    }

    private TableNameResponseDTO convert(TableName entity) {

        TableNameResponseDTO dto = new TableNameResponseDTO();

        dto.setTableNameId(entity.getTableNameId());
        dto.setName(entity.getName());
        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());

        return dto;
    }
}