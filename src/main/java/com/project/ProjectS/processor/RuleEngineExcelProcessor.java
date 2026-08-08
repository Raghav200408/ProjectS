package com.project.ProjectS.processor;

import com.project.ProjectS.entity.*;
import com.project.ProjectS.mapper.RuleEngineExcelMapper;
import com.project.ProjectS.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Component
@Transactional
public class RuleEngineExcelProcessor implements ExcelProcessor {


    @Autowired
    private RuleEngineExcelMapper ruleEngineMapper;


    @Autowired
    private RuleEngineRepository ruleEngineRepository;


    @Autowired
    private ChapterRepository chapterRepository;


    @Autowired
    private TableAttributeRepository tableAttributeRepository;


    @Autowired
    private TableHeaderRepository tableHeaderRepository;


    @Autowired
    private TableNameRepository tableNameRepository;



    @Override
    public void process(List<Map<String, String>> excelData) {


        for (Map<String, String> row : excelData) {


            // Skip empty rows
            if (row.values().stream().allMatch(
                    value -> value == null || value.isBlank()
            )) {
                continue;
            }



            RuleEngine ruleEngine =
                    ruleEngineMapper.map(row);



            /*
             * ==========================
             * Chapter
             * ==========================
             */

            String chapterName =
                    clean(row.get("chapter_name"));


            if (chapterName == null) {

                throw new RuntimeException(
                        "chapter_name is required"
                );
            }



            Chapter chapter =
                    chapterRepository.findByName(chapterName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Chapter not found : "
                                                    + chapterName
                                    )
                            );


            ruleEngine.setChapter(chapter);





            /*
             * ==========================
             * Pair Attribute
             * ==========================
             */

            String pairAttributeName =
                    clean(row.get("pair_attribute_name"));


            if(pairAttributeName != null) {


                TableAttribute attribute =
                        tableAttributeRepository
                                .findByName(pairAttributeName)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Pair Attribute not found : "
                                                        + pairAttributeName
                                        )
                                );


                ruleEngine.setPairAttribute(attribute);

            }







            /*
             * ==========================
             * Field Name
             * ==========================
             */

            String fieldName =
                    clean(row.get("field_name"));


            if(fieldName != null) {


                TableAttribute attribute =
                        tableAttributeRepository
                                .findByName(fieldName)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Field Name not found : "
                                                        + fieldName
                                        )
                                );


                ruleEngine.setTableAttributeid(attribute);

            }







            /*
             * ==========================
             * Field Type
             * ==========================
             */

            String fieldType =
                    clean(row.get("field_type"));


            if(fieldType != null) {


                TableHeader header =
                        tableHeaderRepository
                                .findByName(fieldType)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Field Type not found : "
                                                        + fieldType
                                        )
                                );


               // ruleEngine.setTableAttributeid(header);

            }







            /*
             * ==========================
             * Pair Order
             * ==========================
             */

            String pairOrder =
                    clean(row.get("pair_order"));


            if(pairOrder != null) {

                ruleEngine.setPairOrder(
                        Integer.parseInt(pairOrder)
                );

            }







            /*
             * ==========================
             * Conditions 1 - 4
             * ==========================
             */

            setTableAndHeader(row, ruleEngine, 1);

            setTableAndHeader(row, ruleEngine, 2);

            setTableAndHeader(row, ruleEngine, 3);

            setTableAndHeader(row, ruleEngine, 4);








            /*
             * ==========================
             * Duplicate Check
             * ==========================
             */

             boolean exists=false;
//            boolean exists =  @todo the code changes to check existing record
//                    ruleEngineRepository.existsRule(
//
//                            ruleEngine.getChapter()
//                                    .getChapterId(),
//
//
//                          null,null,
//
//                            ruleEngine.getRelationshipName() != null
//                                    ? ruleEngine.getRelationshipName().trim()
//                                    : ""
//                    );



            if (exists) {


                System.out.println(
                        "Duplicate Rule Skipped : "
                                + ruleEngine.getRelationshipName()
                );


                continue;

            }







            /*
             * ==========================
             * Save Rule Engine
             * ==========================
             */


            System.out.println(
                    "Saving Rule Engine : "
                            + ruleEngine.getRelationshipName()
            );

            RuleEngine savedRule =
                    ruleEngineRepository.save(ruleEngine);

            System.out.println(
                    "========== SAVED RULE ENGINE ID : "
                            + savedRule.getRuleEngineId()
                            + " =========="
            );


        }

    }








    private void setTableAndHeader(
            Map<String,String> row,
            RuleEngine ruleEngine,
            int index
    ) {



        String tableName =
                clean(
                        row.get(
                                "table" + index + "_name"
                        )
                );



        String headerName =
                clean(
                        row.get(
                                "header" + index + "_name"
                        )
                );







        /*
         * Table Mapping
         */

        if(tableName != null) {


            TableName table =
                    tableNameRepository
                            .findByName(tableName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Table not found : "
                                                    + tableName
                                    )
                            );



            switch(index) {

                case 1 ->
                        ruleEngine.setTable1(table);

                case 2 ->
                        ruleEngine.setTable2(table);

                case 3 ->
                        ruleEngine.setTable3(table);

                case 4 ->
                        ruleEngine.setTable4(table);

            }

        }








        /*
         * Header Mapping
         */

        if(headerName != null) {


            TableHeader header =
                    tableHeaderRepository
                            .findByName(headerName)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Header not found : "
                                                    + headerName
                                    )
                            );



            switch(index) {

                case 1 ->
                        ruleEngine.setHeader1(header);

                case 2 ->
                        ruleEngine.setHeader2(header);

                case 3 ->
                        ruleEngine.setHeader3(header);

                case 4 ->
                        ruleEngine.setHeader4(header);

            }

        }


    }








    private String clean(String value) {


        if(value == null) {

            return null;

        }


        value = value.trim();


        return value.isEmpty()
                ? null
                : value;

    }


}