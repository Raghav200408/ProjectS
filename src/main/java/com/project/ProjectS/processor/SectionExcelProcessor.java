package com.project.ProjectS.processor;


import com.project.ProjectS.entity.Section;
import com.project.ProjectS.repository.SectionRepository;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class SectionExcelProcessor {


    private final SectionRepository sectionRepository;



    public SectionExcelProcessor(
            SectionRepository sectionRepository) {

        this.sectionRepository = sectionRepository;
    }



    public void process(List<Section> sections) {


        if (sections == null || sections.isEmpty()) {

            throw new RuntimeException(
                    "Section Excel data is empty"
            );
        }



        sectionRepository.saveAll(sections);

    }

}