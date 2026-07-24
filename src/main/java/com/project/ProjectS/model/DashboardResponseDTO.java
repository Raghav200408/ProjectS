package com.project.ProjectS.model;

public class DashboardResponseDTO {

    private long totalColleges;
    private long totalBranches;
    private long totalCourses;
    private long totalSections;

    public long getTotalColleges() {
        return totalColleges;
    }

    public void setTotalColleges(long totalColleges) {
        this.totalColleges = totalColleges;
    }

    public long getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(long totalBranches) {
        this.totalBranches = totalBranches;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalSections() {
        return totalSections;
    }

    public void setTotalSections(long totalSections) {
        this.totalSections = totalSections;
    }
}