package com.project.ProjectS.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.data.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "sectionId",
    "courseId",
    "courseName",
    "branchName",
    "collegeName",
    "sectionName",
    "description"
})

@Table("section")
public class SectionDTO {

    @Id
    @Column("section_id")
    private Long sectionId;

    @Column("course_id")
    private Long courseId;

    @Column("section_name")
    private String sectionName;

    @Column("description")
    private String description;
    
    @Transient
    private String courseName;

    @Transient
    private String branchName;

    @Transient
    private String collegeName;

    public SectionDTO() {
    }

    public SectionDTO(Long sectionId, Long courseId, String sectionName, String description) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.sectionName = sectionName;
        this.description = description;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }
}