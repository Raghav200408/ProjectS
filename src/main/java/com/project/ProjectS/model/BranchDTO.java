package com.project.ProjectS.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;



@JsonPropertyOrder({
    "branchId",
    "collegeId",
    "collegeName",
    "branchName",
    "address",
    "phoneNumber",
    "email"
})

@Table("branch")
public class BranchDTO {

    @Id
    @Column("branch_id")
    private Long branchId;

    @Column("college_id")
    private Long collegeId;
    
    @Transient
    private String collegeName;

    @Column("branch_name")
    private String branchName;

    @Column("address")
    private String address;

    @Column("phone_number")
    private String phoneNumber;

    @Column("email")
    private String email;
    
   
    

    public BranchDTO() {
    }

    public BranchDTO(Long branchId, Long collegeId, String branchName,
                     String address, String phoneNumber, String email) {
        this.branchId = branchId;
        this.collegeId = collegeId;
        this.branchName = branchName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }
    
    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    
}