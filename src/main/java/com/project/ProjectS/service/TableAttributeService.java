package com.project.ProjectS.service;

import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.model.TableAttributeRequestDTO;
import com.project.ProjectS.model.TableAttributeResponseDTO;
import com.project.ProjectS.repository.TableAttributeRepository;
import com.project.ProjectS.repository.TableHeaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TableAttributeService {

    @Autowired
    private TableAttributeRepository attributeRepository;

    @Autowired
    private TableHeaderRepository headerRepository;

    public String create(TableAttributeRequestDTO request) {

        if(attributeRepository.existsByName(request.getName())){
            throw new RuntimeException("Table Attribute already exists");
        }

        TableHeader header = headerRepository.findByName(request.getTableHeaderName())
                .orElseThrow(() -> new RuntimeException("Table Header not found"));

        TableAttribute entity = new TableAttribute();
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());
        entity.setTableHeader(header);

        attributeRepository.save(entity);

        return "Table Attribute created successfully";
    }

    public List<TableAttributeResponseDTO> getAll() {

        List<TableAttribute> attributes = attributeRepository.findAll();
        List<TableAttributeResponseDTO> response = new ArrayList<>();

        for(TableAttribute entity : attributes){

            TableAttributeResponseDTO dto = new TableAttributeResponseDTO();

            dto.setAttributeId(entity.getAttributeId());
            dto.setName(entity.getName());
            dto.setShortName(entity.getShortName());
            dto.setActiveRow(entity.getActiveRow());
            dto.setRowStatus(entity.getRowStatus());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedAt(entity.getUpdatedAt());
            dto.setTableHeaderName(entity.getTableHeader().getName());

            response.add(dto);
        }

        return response;
    }

    public TableAttributeResponseDTO getById(Long id) {

        TableAttribute entity = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Attribute not found"));

        TableAttributeResponseDTO dto = new TableAttributeResponseDTO();

        dto.setAttributeId(entity.getAttributeId());
        dto.setName(entity.getName());
        dto.setShortName(entity.getShortName());
        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setTableHeaderName(entity.getTableHeader().getName());

        return dto;
    }

    public String update(Long id, TableAttributeRequestDTO request) {

        TableAttribute entity = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Attribute not found"));

        TableHeader header = headerRepository.findByName(request.getTableHeaderName())
                .orElseThrow(() -> new RuntimeException("Table Header not found"));

        entity.setName(request.getName());
        entity.setShortName(request.getShortName());
        entity.setTableHeader(header);

        attributeRepository.save(entity);

        return "Table Attribute updated successfully";
    }

    public String delete(Long id) {

        TableAttribute entity = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table Attribute not found"));

        attributeRepository.delete(entity);

        return "Table Attribute deleted successfully";
    }

}