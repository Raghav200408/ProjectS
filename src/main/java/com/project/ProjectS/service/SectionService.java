package com.project.ProjectS.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.SectionDTO;
import com.project.ProjectS.repository.SectionDAO;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class SectionService {

    @Autowired
    private SectionDAO sectionDAO;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SectionDTO saveSection(SectionDTO section) {
        return sectionDAO.save(section);
    }

    public List<SectionDTO> getAllSections() {

        String sql = """
            SELECT
                s.section_id,
                s.course_id,
                c.course_name,
                b.branch_name,
                col.institute_name AS college_name,
                s.section_name,
                s.description
            FROM section s
            INNER JOIN course c
                ON s.course_id = c.course_id
            INNER JOIN branch b
                ON c.branch_id = b.branch_id
            INNER JOIN college col
                ON b.college_id = col.college_id
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            SectionDTO section = new SectionDTO();

            section.setSectionId(rs.getLong("section_id"));
            section.setCourseId(rs.getLong("course_id"));
            section.setCourseName(rs.getString("course_name"));
            section.setBranchName(rs.getString("branch_name"));
            section.setCollegeName(rs.getString("college_name"));
            section.setSectionName(rs.getString("section_name"));
            section.setDescription(rs.getString("description"));

            return section;
        });
    }

    public SectionDTO getSectionById(Long id) {

        String sql = """
            SELECT
                s.section_id,
                s.course_id,
                c.course_name,
                b.branch_name,
                col.institute_name AS college_name,
                s.section_name,
                s.description
            FROM section s
            INNER JOIN course c
                ON s.course_id = c.course_id
            INNER JOIN branch b
                ON c.branch_id = b.branch_id
            INNER JOIN college col
                ON b.college_id = col.college_id
            WHERE s.section_id = ?
            """;

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {

            SectionDTO section = new SectionDTO();

            section.setSectionId(rs.getLong("section_id"));
            section.setCourseId(rs.getLong("course_id"));
            section.setCourseName(rs.getString("course_name"));
            section.setBranchName(rs.getString("branch_name"));
            section.setCollegeName(rs.getString("college_name"));
            section.setSectionName(rs.getString("section_name"));
            section.setDescription(rs.getString("description"));

            return section;
        });
    }

    public SectionDTO updateSection(SectionDTO section) {

        sectionDAO.save(section);

        return getSectionById(section.getSectionId());
    }

    public void deleteSection(Long id) {
        sectionDAO.deleteById(id);
    }
}