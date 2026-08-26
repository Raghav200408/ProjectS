package com.project.ProjectS.service;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.RuleEngine;
import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.entity.TableName;
import com.project.ProjectS.mapper.RuleEngineMapper;
import com.project.ProjectS.model.RuleEngineRequestDTO;
import com.project.ProjectS.model.RuleEngineResponse;
import com.project.ProjectS.model.RuleEngineResponseDTO;
import com.project.ProjectS.repository.ChapterRepository;
import com.project.ProjectS.repository.RuleEngineRepository;
import com.project.ProjectS.repository.TableAttributeRepository;
import com.project.ProjectS.repository.TableHeaderRepository;
import com.project.ProjectS.repository.TableNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class RuleEngineService {
    @Autowired
    public RuleEngineService(RuleEngineRepository ruleEngineRepository, ChapterRepository chapterRepository, TableAttributeRepository tableAttributeRepository, TableNameRepository tableNameRepository, TableHeaderRepository tableHeaderRepository,RuleEngineMapper mapper) {
        this.ruleEngineRepository = ruleEngineRepository;
        this.chapterRepository = chapterRepository;
        this.tableAttributeRepository = tableAttributeRepository;
        this.tableNameRepository = tableNameRepository;
        this.tableHeaderRepository = tableHeaderRepository;
        this.mapper = mapper;
    }

    private final RuleEngineRepository ruleEngineRepository;
    private final ChapterRepository chapterRepository;
    private final TableAttributeRepository tableAttributeRepository;
    private final TableNameRepository tableNameRepository;
    private final TableHeaderRepository tableHeaderRepository;
    private final  RuleEngineMapper mapper;


    public String create(RuleEngineRequestDTO request) {

        RuleEngine entity = new RuleEngine();
        mapRequest(entity, request);

        ruleEngineRepository.save(entity);


        if (entity.getTableAttributeid() != null) {
            entity.getTableAttributeid().setRowStatus("RULE");
            tableAttributeRepository.save(entity.getTableAttributeid());
        }

        return "Rule Engine created successfully";
    }

    public List<RuleEngineResponseDTO> getAll() {

        return ruleEngineRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public RuleEngineResponseDTO getById(Long id) {

        RuleEngine entity = ruleEngineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule Engine not found"));

        return convert(entity);
    }

    public String update(Long id, RuleEngineRequestDTO request) {

        RuleEngine entity = ruleEngineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule Engine not found"));

        mapRequest(entity, request);
        ruleEngineRepository.save(entity);

        return "Rule Engine updated successfully";
    }

    public String delete(Long id) {

        RuleEngine entity = ruleEngineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule Engine not found"));

        ruleEngineRepository.delete(entity);

        return "Rule Engine deleted successfully";
    }

    private void mapRequest(RuleEngine entity, RuleEngineRequestDTO request) {

        Chapter chapter = getChapter(request.getChapterName());

        entity.setChapter(chapter);
        entity.setPairAttribute(getTableAttribute(request.getPairAttributeName(), "Pair Attribute"));
//        entity.setFieldName(getTableAttribute(request.getFieldName(), "Field Name"));
//        entity.setFieldType(getTableHeader(request.getFieldType(), "Field Type"));
        entity.setTableAttributeid(getTableAttribute(request.getAttributeName(), "Attribute"));
        entity.setRelationshipName(request.getRelationshipName());
        entity.setPairOrder(request.getPairOrder());
        entity.setArithmetic1(request.getArithmetic1());
        entity.setTable1(getTableName(request.getTable1Name(), "Table 1"));
        entity.setHeader1(getTableHeader(request.getHeader1Name(), "Header 1"));
        entity.setAmountPosition1(request.getAmountPosition1());
        entity.setInformation1(request.getInformation1());

        entity.setArithmetic2(request.getArithmetic2());
        entity.setTable2(getTableName(request.getTable2Name(), "Table 2"));
        entity.setHeader2(getTableHeader(request.getHeader2Name(), "Header 2"));
        entity.setAmountPosition2(request.getAmountPosition2());
        entity.setInformation2(request.getInformation2());

        entity.setArithmetic3(request.getArithmetic3());
        entity.setTable3(getTableName(request.getTable3Name(), "Table 3"));
        entity.setHeader3(getTableHeader(request.getHeader3Name(), "Header 3"));
        entity.setAmountPosition3(request.getAmountPosition3());
        entity.setInformation3(request.getInformation3());

        entity.setArithmetic4(request.getArithmetic4());
        entity.setTable4(getTableName(request.getTable4Name(), "Table 4"));
        entity.setHeader4(getTableHeader(request.getHeader4Name(), "Header 4"));
        entity.setAmountPosition4(request.getAmountPosition4());
        entity.setInformation4(request.getInformation4());

//        entity.setActiveRow(request.getActiveRow());
//        entity.setRowStatus(request.getRowStatus());
        entity.setActiveRow(request.getActiveRow());
        if (request.getRowStatus() != null) {
            entity.setRowStatus(request.getRowStatus());
        }
    }

    private Chapter getChapter(String name) {

        if (hasText(name)) {
            String chapterName = name.trim();
            return chapterRepository.findByName(chapterName)
                    .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterName));
        }

        throw new RuntimeException("Chapter name is required");
    }

    private TableAttribute getTableAttribute(String name, String fieldLabel) {

        if (hasText(name)) {
            String attributeName = name.trim();
            return tableAttributeRepository.findByName(attributeName)
                    .orElseThrow(() -> new RuntimeException(fieldLabel + " not found: " + attributeName));
        }

        return null;
    }

    private TableName getTableName(String name, String fieldLabel) {

        if (hasText(name)) {
            String tableName = name.trim();
            return tableNameRepository.findByName(tableName)
                    .orElseThrow(() -> new RuntimeException(fieldLabel + " not found: " + tableName));
        }

        return null;
    }

    private TableHeader getTableHeader(String name, String fieldLabel) {

        if (hasText(name)) {
            String headerName = name.trim();
            return tableHeaderRepository.findByName(headerName)
                    .orElseThrow(() -> new RuntimeException(fieldLabel + " not found: " + headerName));
        }

        return null;
    }

    private boolean hasText(String value) {

        return value != null && !value.trim().isEmpty();
    }

    private RuleEngineResponseDTO convert(RuleEngine entity) {

        RuleEngineResponseDTO dto = new RuleEngineResponseDTO();

        dto.setRuleEngineId(entity.getRuleEngineId());
        dto.setChapterId(entity.getChapter().getChapterId());
        dto.setChapterName(entity.getChapter().getName());
        dto.setPairAttributeId(getAttributeId(entity.getPairAttribute()));
        dto.setPairAttributeName(getAttributeName(entity.getPairAttribute()));
        dto.setAttributeId(
                getAttributeId(entity.getTableAttributeid())
        );

        dto.setAttributeName(
                getAttributeName(entity.getTableAttributeid())
        );
        dto.setPairAttributeTableHeaderName(getAttributeHeaderName(entity.getPairAttribute()));
//        dto.setFieldName(getAttributeName(entity.getFieldName()));
//        dto.setFieldType(getHeaderName(entity.getFieldType()));
        dto.setRelationshipName(entity.getRelationshipName());
        dto.setPairOrder(entity.getPairOrder());

        dto.setArithmetic1(entity.getArithmetic1());
        dto.setTable1Id(getTableId(entity.getTable1()));
        dto.setTable1Name(getTableName(entity.getTable1()));
        dto.setHeader1Id(getHeaderId(entity.getHeader1()));
        dto.setHeader1Name(getHeaderName(entity.getHeader1()));
        dto.setAmountPosition1(entity.getAmountPosition1());
        dto.setInformation1(entity.getInformation1());

        dto.setArithmetic2(entity.getArithmetic2());
        dto.setTable2Id(getTableId(entity.getTable2()));
        dto.setTable2Name(getTableName(entity.getTable2()));
        dto.setHeader2Id(getHeaderId(entity.getHeader2()));
        dto.setHeader2Name(getHeaderName(entity.getHeader2()));
        dto.setAmountPosition2(entity.getAmountPosition2());
        dto.setInformation2(entity.getInformation2());

        dto.setArithmetic3(entity.getArithmetic3());
        dto.setTable3Id(getTableId(entity.getTable3()));
        dto.setTable3Name(getTableName(entity.getTable3()));
        dto.setHeader3Id(getHeaderId(entity.getHeader3()));
        dto.setHeader3Name(getHeaderName(entity.getHeader3()));
        dto.setAmountPosition3(entity.getAmountPosition3());
        dto.setInformation3(entity.getInformation3());

        dto.setArithmetic4(entity.getArithmetic4());
        dto.setTable4Id(getTableId(entity.getTable4()));
        dto.setTable4Name(getTableName(entity.getTable4()));
        dto.setHeader4Id(getHeaderId(entity.getHeader4()));
        dto.setHeader4Name(getHeaderName(entity.getHeader4()));
        dto.setAmountPosition4(entity.getAmountPosition4());
        dto.setInformation4(entity.getInformation4());

        dto.setActiveRow(entity.getActiveRow());
        dto.setRowStatus(entity.getRowStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    private Long getAttributeId(TableAttribute attribute) {

        return attribute != null ? attribute.getAttributeId() : null;
    }

    private String getAttributeName(TableAttribute attribute) {

        return attribute != null ? attribute.getName() : null;
    }



    private String getAttributeHeaderName(TableAttribute attribute) {

        return attribute != null && attribute.getTableHeader() != null
                ? attribute.getTableHeader().getName()
                : null;
    }

    private Long getTableId(TableName tableName) {

        return tableName != null ? tableName.getTableNameId() : null;
    }

    private String getTableName(TableName tableName) {

        return tableName != null ? tableName.getName() : null;
    }

    private Long getHeaderId(TableHeader header) {

        return header != null ? header.getHeaderId() : null;
    }

    private String getHeaderName(TableHeader header) {

        return header != null ? header.getName() : null;
    }

    public List<RuleEngineResponse> getRuleEngineByAttributeId(Long attributeId) {

        return ruleEngineRepository.findByAttributeId(attributeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

