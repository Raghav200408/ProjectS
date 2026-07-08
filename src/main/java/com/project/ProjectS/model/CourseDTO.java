package com.project.ProjectS.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({
    "courseId",
    "branchId",
    "branchName",
    "collegeName",
    "courseName",
    "description"
})
@Table("course")
public class CourseDTO {

    @Id
    @Column("course_id")
    private Long courseId;
    
    @Column("branch_id")
    private Long branchId;
    
    @Transient
    private String branchName;

    @Transient
    private String collegeName;

    @Column("course_name")
    private String courseName;

    @Column("description")
    private String description;

    public CourseDTO() {
    }

    public CourseDTO(Long courseId, Long branchId, String courseName, String description) {
        this.courseId = courseId;
        this.branchId = branchId;
        this.courseName = courseName;
        this.description = description;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
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
    
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}