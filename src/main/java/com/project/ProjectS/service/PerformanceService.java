package com.project.ProjectS.service;


import com.project.ProjectS.entity.Exam;
import com.project.ProjectS.entity.ExamResult;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.*;
import com.project.ProjectS.repository.ExamRepository;
import com.project.ProjectS.repository.ExamResultRepository;
import com.project.ProjectS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    public PerformanceService(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            UserRepository userRepository) {

        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
    }

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;


    /*
     * ============================================================
     * COLLEGE ADMIN PERFORMANCE
     * ============================================================
     */
    public PerformanceDashboardResponseDTO getCollegePerformance(
            Authentication authentication,
            Long branchId,
            Long courseId,
            Long sectionId,
            Long examId) {

        User admin = getLoggedInUser(authentication);

        if (admin.getCollege() == null) {
            throw new RuntimeException(
                    "College is not assigned to this user");
        }

        Long collegeId = admin.getCollege().getCollegeId();

        /*
         * ============================================================
         * GET TOTAL EXAMS FROM EXAM TABLE
         * ============================================================
         */

        List<Exam> exams =
                examRepository.findByCollege_CollegeId(collegeId);

        // Apply filters
        exams = exams.stream()
                .filter(exam ->
                        branchId == null ||
                                (exam.getBranch() != null &&
                                        exam.getBranch().getBranchId().equals(branchId)))
                .filter(exam ->
                        courseId == null ||
                                (exam.getCourse() != null &&
                                        exam.getCourse().getCourseId().equals(courseId)))
                .filter(exam ->
                        sectionId == null ||
                                (exam.getSection() != null &&
                                        exam.getSection().getSectionId().equals(sectionId)))
                .filter(exam ->
                        examId == null ||
                                exam.getExamId().equals(examId))
                .toList();

        int examsConducted = exams.size();


        /*
         * ============================================================
         * GET RESULTS FOR PERFORMANCE CALCULATIONS
         * ============================================================
         */

        List<ExamResult> results =
                examResultRepository.findByExam_College_CollegeId(collegeId);

        // Apply same filters to results
        results = results.stream()
                .filter(result -> {

                    Exam exam = result.getExam();

                    if (branchId != null &&
                            (exam.getBranch() == null ||
                                    !exam.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }

                    if (courseId != null &&
                            (exam.getCourse() == null ||
                                    !exam.getCourse().getCourseId().equals(courseId))) {
                        return false;
                    }

                    if (sectionId != null &&
                            (exam.getSection() == null ||
                                    !exam.getSection().getSectionId().equals(sectionId))) {
                        return false;
                    }

                    if (examId != null &&
                            !exam.getExamId().equals(examId)) {
                        return false;
                    }

                    return true;
                })
                .toList();


        return buildPerformanceDashboard(
                results,
                examsConducted
        );
    }

    public PerformanceDashboardResponseDTO getSuperAdminPerformance(
            Authentication authentication,
            Long collegeId,
            Long branchId,
            Long courseId,
            Long sectionId,
            Long examId) {

        User admin = getLoggedInUser(authentication);

        if (admin.getRole() == null ||
                !"SUPER_ADMIN".equalsIgnoreCase(
                        admin.getRole().getRoleName())) {

            throw new RuntimeException("Access denied");
        }


        /*
         * ============================================================
         * GET ALL EXAMS
         * ============================================================
         */

        List<Exam> exams = examRepository.findAll();

        // Apply filters
        exams = exams.stream()
                .filter(exam ->
                        collegeId == null ||
                                (exam.getCollege() != null &&
                                        exam.getCollege().getCollegeId().equals(collegeId)))
                .filter(exam ->
                        branchId == null ||
                                (exam.getBranch() != null &&
                                        exam.getBranch().getBranchId().equals(branchId)))
                .filter(exam ->
                        courseId == null ||
                                (exam.getCourse() != null &&
                                        exam.getCourse().getCourseId().equals(courseId)))
                .filter(exam ->
                        sectionId == null ||
                                (exam.getSection() != null &&
                                        exam.getSection().getSectionId().equals(sectionId)))
                .filter(exam ->
                        examId == null ||
                                exam.getExamId().equals(examId))
                .toList();

        int examsConducted = exams.size();


        /*
         * ============================================================
         * GET RESULTS
         * ============================================================
         */

        List<ExamResult> results =
                examResultRepository.findAll();

        // Apply same filters to results
        results = results.stream()
                .filter(result -> {

                    Exam exam = result.getExam();

                    if (collegeId != null &&
                            (exam.getCollege() == null ||
                                    !exam.getCollege().getCollegeId().equals(collegeId))) {
                        return false;
                    }

                    if (branchId != null &&
                            (exam.getBranch() == null ||
                                    !exam.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }

                    if (courseId != null &&
                            (exam.getCourse() == null ||
                                    !exam.getCourse().getCourseId().equals(courseId))) {
                        return false;
                    }

                    if (sectionId != null &&
                            (exam.getSection() == null ||
                                    !exam.getSection().getSectionId().equals(sectionId))) {
                        return false;
                    }

                    if (examId != null &&
                            !exam.getExamId().equals(examId)) {
                        return false;
                    }

                    return true;
                })
                .toList();


        return buildPerformanceDashboard(
                results,
                examsConducted
        );
    }


    public PerformanceDashboardResponseDTO getBranchPerformance(
            Authentication authentication,
            Long courseId,
            Long sectionId,
            Long examId) {

        User admin = getLoggedInUser(authentication);

        if (admin.getBranch() == null) {
            throw new RuntimeException(
                    "Branch is not assigned to this user");
        }

        Long branchId = admin.getBranch().getBranchId();


        /*
         * ============================================================
         * GET TOTAL EXAMS FROM EXAM TABLE
         * ============================================================
         */

        List<Exam> exams =
                examRepository.findByBranch_BranchId(branchId);

        // Apply filters
        exams = exams.stream()
                .filter(exam ->
                        courseId == null ||
                                (exam.getCourse() != null &&
                                        exam.getCourse().getCourseId().equals(courseId)))
                .filter(exam ->
                        sectionId == null ||
                                (exam.getSection() != null &&
                                        exam.getSection().getSectionId().equals(sectionId)))
                .filter(exam ->
                        examId == null ||
                                exam.getExamId().equals(examId))
                .toList();

        int examsConducted = exams.size();


        /*
         * ============================================================
         * GET RESULTS
         * ============================================================
         */

        List<ExamResult> results =
                examResultRepository.findByExam_Branch_BranchId(branchId);

        // Apply same filters
        results = results.stream()
                .filter(result -> {

                    Exam exam = result.getExam();

                    if (courseId != null &&
                            (exam.getCourse() == null ||
                                    !exam.getCourse().getCourseId().equals(courseId))) {
                        return false;
                    }

                    if (sectionId != null &&
                            (exam.getSection() == null ||
                                    !exam.getSection().getSectionId().equals(sectionId))) {
                        return false;
                    }

                    if (examId != null &&
                            !exam.getExamId().equals(examId)) {
                        return false;
                    }

                    return true;
                })
                .toList();


        return buildPerformanceDashboard(
                results,
                examsConducted
        );
    }

    /*
     * ============================================================
     * STUDENT PERFORMANCE
     * ============================================================
     */
    public StudentPerformanceDTO getStudentPerformance(
            Authentication authentication) {

        User student = getLoggedInUser(authentication);

        if (student.getRole() == null ||
                !"STUDENT".equalsIgnoreCase(
                        student.getRole().getRoleName())) {

            throw new RuntimeException("Access denied");
        }

        Long userId = student.getUserId();

        List<ExamResult> results =
                examResultRepository
                        .findByUser_UserIdOrderByCreatedAtAsc(userId);

        StudentPerformanceDTO response =
                new StudentPerformanceDTO();

        response.setUserId(student.getUserId());
        response.setStudentName(student.getName());

        /*
         * ============================================================
         * COLLEGE
         * ============================================================
         */

        if (student.getCollege() != null) {

            response.setCollegeId(
                    student.getCollege().getCollegeId()
            );

            response.setCollegeName(
                    student.getCollege().getInstituteName()
            );
        }

        /*
         * ============================================================
         * BRANCH
         * ============================================================
         */

        if (student.getBranch() != null) {

            response.setBranchId(
                    student.getBranch().getBranchId()
            );

            response.setBranchName(
                    student.getBranch().getBranchName()
            );
        }

        /*
         * ============================================================
         * SECTION
         * ============================================================
         */

        if (student.getSection() != null) {

            response.setSectionId(
                    student.getSection().getSectionId()
            );

            response.setSectionName(
                    student.getSection().getSectionName()
            );
        }

        /*
         * ============================================================
         * NO EXAM RESULTS
         * ============================================================
         */

        if (results == null || results.isEmpty()) {

            response.setExamsAttempted(0);
            response.setAveragePercentage(0.0);
            response.setHighestPercentage(0.0);
            response.setLowestPercentage(0.0);
            response.setPassedExams(0);
            response.setFailedExams(0);
            response.setPassRate(0.0);

            return response;
        }

        /*
         * ============================================================
         * LATEST RESULT FOR EACH EXAM
         * ============================================================
         */

        Map<Long, ExamResult> latestResultByExam =
                results.stream()
                        .filter(result -> result.getExam() != null)
                        .collect(Collectors.toMap(
                                result -> result.getExam().getExamId(),
                                result -> result,
                                (oldResult, newResult) -> newResult
                        ));

        List<ExamResult> latestResults =
                new ArrayList<>(latestResultByExam.values());

        /*
         * ============================================================
         * EXAMS ATTEMPTED
         * ============================================================
         */

        int examsAttempted = latestResults.size();

        /*
         * ============================================================
         * AVERAGE
         * ============================================================
         */

        double averagePercentage =
                latestResults.stream()
                        .mapToDouble(ExamResult::getPercentage)
                        .average()
                        .orElse(0.0);

        /*
         * ============================================================
         * HIGHEST
         * ============================================================
         */

        double highestPercentage =
                latestResults.stream()
                        .mapToDouble(ExamResult::getPercentage)
                        .max()
                        .orElse(0.0);

        /*
         * ============================================================
         * LOWEST
         * ============================================================
         */

        double lowestPercentage =
                latestResults.stream()
                        .mapToDouble(ExamResult::getPercentage)
                        .min()
                        .orElse(0.0);

        /*
         * ============================================================
         * PASSED / FAILED
         * ============================================================
         */

        int passedExams = (int) latestResults.stream()
                .filter(this::isPassed)
                .count();

        int failedExams =
                latestResults.size() - passedExams;

        /*
         * ============================================================
         * PASS RATE
         * ============================================================
         */

        double passRate =
                examsAttempted == 0
                        ? 0.0
                        : ((double) passedExams /
                        examsAttempted) * 100;

        /*
         * ============================================================
         * SET PERFORMANCE
         * ============================================================
         */

        response.setExamsAttempted(examsAttempted);

        response.setAveragePercentage(
                round(averagePercentage)
        );

        response.setHighestPercentage(
                round(highestPercentage)
        );

        response.setLowestPercentage(
                round(lowestPercentage)
        );

        response.setPassedExams(passedExams);

        response.setFailedExams(failedExams);

        response.setPassRate(
                round(passRate)
        );

        return response;
    }


    /*
     * ============================================================
     * BUILD PERFORMANCE DASHBOARD
     * ============================================================
     */
    private PerformanceDashboardResponseDTO buildPerformanceDashboard(
            List<ExamResult> results,
            int examsConducted) {

        PerformanceDashboardResponseDTO response =
                new PerformanceDashboardResponseDTO();

        if (results == null || results.isEmpty()) {

            response.setTotalStudents(0);
            response.setExamsConducted(examsConducted);
            response.setAveragePercentage(0.0);
            response.setPassRate(0.0);
            response.setPassedResults(0);
            response.setFailedResults(0);

            response.setBranchPerformance(new ArrayList<>());
            response.setCoursePerformance(new ArrayList<>());
            response.setPerformanceTrend(new ArrayList<>());
            response.setTopPerformers(new ArrayList<>());
            response.setStudentsNeedingAttention(new ArrayList<>());

            return response;
        }


        /*
         * --------------------------------------------------------
         * TOTAL STUDENTS
         * --------------------------------------------------------
         */
        int totalStudents = (int) results.stream()
                .map(result -> result.getUser().getUserId())
                .distinct()
                .count();





        /*
         * --------------------------------------------------------
         * AVERAGE PERCENTAGE
         * --------------------------------------------------------
         */
        double averagePercentage = results.stream()
                .mapToDouble(ExamResult::getPercentage)
                .average()
                .orElse(0.0);


        /*
         * --------------------------------------------------------
         * PASSED / FAILED
         * --------------------------------------------------------
         */
        int passedResults = 0;
        int failedResults = 0;

        for (ExamResult result : results) {

            if (isPassed(result)) {
                passedResults++;
            } else {
                failedResults++;
            }
        }


        /*
         * --------------------------------------------------------
         * PASS RATE
         * --------------------------------------------------------
         */
        double passRate = results.isEmpty()
                ? 0.0
                : ((double) passedResults / results.size()) * 100;


        /*
         * --------------------------------------------------------
         * SET SUMMARY
         * --------------------------------------------------------
         */
        response.setTotalStudents(totalStudents);
        response.setExamsConducted(examsConducted);
        response.setAveragePercentage(round(averagePercentage));
        response.setPassRate(round(passRate));
        response.setPassedResults(passedResults);
        response.setFailedResults(failedResults);


        /*
         * --------------------------------------------------------
         * BRANCH PERFORMANCE
         * --------------------------------------------------------
         */
        response.setBranchPerformance(
                buildBranchPerformance(results)
        );


        /*
         * --------------------------------------------------------
         * COURSE PERFORMANCE
         * --------------------------------------------------------
         */
        response.setCoursePerformance(
                buildCoursePerformance(results)
        );


        /*
         * --------------------------------------------------------
         * PERFORMANCE TREND
         * --------------------------------------------------------
         */
        response.setPerformanceTrend(
                buildPerformanceTrend(results)
        );


        response.setTopPerformers(
                buildStudentPerformance(results, true)
        );


        response.setStudentsNeedingAttention(
                buildStudentPerformance(results, false)
        );

        return response;
    }


    /*
     * ============================================================
     * CHECK PASS / FAIL
     * ============================================================
     */
    private boolean isPassed(ExamResult result) {

        if (result.getExam() == null) {
            return false;
        }

        Integer passPercentage =
                result.getExam().getPassPercentage();

        if (passPercentage == null) {
            return false;
        }

        return result.getPercentage() >= passPercentage;
    }


    /*
     * ============================================================
     * BRANCH PERFORMANCE
     * ============================================================
     */
    private List<BranchPerformanceDTO> buildBranchPerformance(
            List<ExamResult> results) {

        Map<Long, List<ExamResult>> grouped =
                results.stream()
                        .filter(result ->
                                result.getExam() != null &&
                                        result.getExam().getBranch() != null)
                        .collect(Collectors.groupingBy(
                                result ->
                                        result.getExam()
                                                .getBranch()
                                                .getBranchId()
                        ));

        List<BranchPerformanceDTO> response =
                new ArrayList<>();

        for (Map.Entry<Long, List<ExamResult>> entry :
                grouped.entrySet()) {

            List<ExamResult> branchResults =
                    entry.getValue();

            BranchPerformanceDTO dto =
                    new BranchPerformanceDTO();

            dto.setBranchId(entry.getKey());

            dto.setBranchName(
                    branchResults.get(0)
                            .getExam()
                            .getBranch()
                            .getBranchName()
            );

            dto.setTotalStudents(
                    (int) branchResults.stream()
                            .map(r -> r.getUser().getUserId())
                            .distinct()
                            .count()
            );

            dto.setExamsConducted(
                    (int) branchResults.stream()
                            .map(r -> r.getExam().getExamId())
                            .distinct()
                            .count()
            );

            dto.setAveragePercentage(
                    round(
                            branchResults.stream()
                                    .mapToDouble(
                                            ExamResult::getPercentage)
                                    .average()
                                    .orElse(0.0)
                    )
            );

            int passed = (int) branchResults.stream()
                    .filter(this::isPassed)
                    .count();

            int failed =
                    branchResults.size() - passed;

            dto.setPassedResults(passed);
            dto.setFailedResults(failed);

            dto.setPassRate(
                    round(
                            branchResults.isEmpty()
                                    ? 0.0
                                    : ((double) passed /
                                    branchResults.size()) * 100
                    )
            );

            response.add(dto);
        }

        return response;
    }


    /*
     * ============================================================
     * COURSE PERFORMANCE
     * ============================================================
     */
    private List<CoursePerformanceDTO> buildCoursePerformance(
            List<ExamResult> results) {

        Map<Long, List<ExamResult>> grouped =
                results.stream()
                        .filter(result ->
                                result.getExam() != null &&
                                        result.getExam().getCourse() != null)
                        .collect(Collectors.groupingBy(
                                result ->
                                        result.getExam()
                                                .getCourse()
                                                .getCourseId()
                        ));

        List<CoursePerformanceDTO> response =
                new ArrayList<>();

        for (Map.Entry<Long, List<ExamResult>> entry :
                grouped.entrySet()) {

            List<ExamResult> courseResults =
                    entry.getValue();

            CoursePerformanceDTO dto =
                    new CoursePerformanceDTO();

            dto.setCourseId(entry.getKey());

            dto.setCourseName(
                    courseResults.get(0)
                            .getExam()
                            .getCourse()
                            .getName()
            );

            dto.setTotalStudents(
                    (int) courseResults.stream()
                            .map(r -> r.getUser().getUserId())
                            .distinct()
                            .count()
            );

            dto.setExamsConducted(
                    (int) courseResults.stream()
                            .map(r -> r.getExam().getExamId())
                            .distinct()
                            .count()
            );

            dto.setAveragePercentage(
                    round(
                            courseResults.stream()
                                    .mapToDouble(
                                            ExamResult::getPercentage)
                                    .average()
                                    .orElse(0.0)
                    )
            );

            int passed = (int) courseResults.stream()
                    .filter(this::isPassed)
                    .count();

            int failed =
                    courseResults.size() - passed;

            dto.setPassedResults(passed);
            dto.setFailedResults(failed);

            dto.setPassRate(
                    round(
                            courseResults.isEmpty()
                                    ? 0.0
                                    : ((double) passed /
                                    courseResults.size()) * 100
                    )
            );

            response.add(dto);
        }

        return response;
    }


    /*
     * ============================================================
     * PERFORMANCE TREND
     * ============================================================
     */
    private List<PerformanceTrendDTO> buildPerformanceTrend(
            List<ExamResult> results) {

        Map<Long, List<ExamResult>> grouped =
                results.stream()
                        .filter(result ->
                                result.getExam() != null)
                        .collect(Collectors.groupingBy(
                                result ->
                                        result.getExam().getExamId()
                        ));

        List<PerformanceTrendDTO> response =
                new ArrayList<>();

        for (List<ExamResult> examResults :
                grouped.values()) {

            ExamResult first =
                    examResults.get(0);

            PerformanceTrendDTO dto =
                    new PerformanceTrendDTO();

            dto.setExamName(
                    first.getExam().getExamName()
            );

            dto.setAveragePercentage(
                    round(
                            examResults.stream()
                                    .mapToDouble(
                                            ExamResult::getPercentage)
                                    .average()
                                    .orElse(0.0)
                    )
            );

            dto.setExamDate(
                    first.getExam().getStartDate()
            );

            response.add(dto);
        }

        response.sort(
                Comparator.comparing(
                        PerformanceTrendDTO::getExamDate,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
        );

        return response;
    }


    /*
     * ============================================================
     * STUDENT PERFORMANCE
     * ============================================================
     */
    private List<StudentPerformanceDTO> buildStudentPerformance(
            List<ExamResult> results,
            boolean topPerformers) {

        Map<Long, List<ExamResult>> grouped =
                results.stream()
                        .filter(result -> result.getUser() != null)
                        .collect(Collectors.groupingBy(
                                result -> result.getUser().getUserId()
                        ));

        List<StudentPerformanceDTO> response =
                new ArrayList<>();

        for (Map.Entry<Long, List<ExamResult>> entry :
                grouped.entrySet()) {

            List<ExamResult> studentResults =
                    entry.getValue();

            User student =
                    studentResults.get(0).getUser();

            StudentPerformanceDTO dto =
                    new StudentPerformanceDTO();

            /*
             * ============================================================
             * STUDENT
             * ============================================================
             */

            dto.setUserId(student.getUserId());
            dto.setStudentName(student.getName());

            /*
             * ============================================================
             * COLLEGE
             * ============================================================
             */

            if (student.getCollege() != null) {

                dto.setCollegeId(
                        student.getCollege().getCollegeId()
                );

                dto.setCollegeName(
                        student.getCollege().getInstituteName()
                );
            }

            /*
             * ============================================================
             * BRANCH
             * ============================================================
             */

            if (student.getBranch() != null) {

                dto.setBranchId(
                        student.getBranch().getBranchId()
                );

                dto.setBranchName(
                        student.getBranch().getBranchName()
                );
            }

            /*
             * ============================================================
             * SECTION
             * ============================================================
             */

            if (student.getSection() != null) {

                dto.setSectionId(
                        student.getSection().getSectionId()
                );

                dto.setSectionName(
                        student.getSection().getSectionName()
                );
            }

            /*
             * ============================================================
             * EXAMS ATTEMPTED
             * ============================================================
             */

            dto.setExamsAttempted(
                    studentResults.size()
            );

            /*
             * ============================================================
             * AVERAGE
             * ============================================================
             */

            double average =
                    studentResults.stream()
                            .mapToDouble(
                                    ExamResult::getPercentage
                            )
                            .average()
                            .orElse(0.0);

            /*
             * ============================================================
             * HIGHEST
             * ============================================================
             */

            double highest =
                    studentResults.stream()
                            .mapToDouble(
                                    ExamResult::getPercentage
                            )
                            .max()
                            .orElse(0.0);

            /*
             * ============================================================
             * LOWEST
             * ============================================================
             */

            double lowest =
                    studentResults.stream()
                            .mapToDouble(
                                    ExamResult::getPercentage
                            )
                            .min()
                            .orElse(0.0);

            /*
             * ============================================================
             * PASSED / FAILED
             * ============================================================
             */

            int passed = (int) studentResults.stream()
                    .filter(this::isPassed)
                    .count();

            int failed =
                    studentResults.size() - passed;

            /*
             * ============================================================
             * SET PERFORMANCE
             * ============================================================
             */

            dto.setAveragePercentage(
                    round(average)
            );

            dto.setHighestPercentage(
                    round(highest)
            );

            dto.setLowestPercentage(
                    round(lowest)
            );

            dto.setPassedExams(passed);

            dto.setFailedExams(failed);

            /*
             * ============================================================
             * PASS RATE
             * ============================================================
             */

            dto.setPassRate(
                    round(
                            studentResults.isEmpty()
                                    ? 0.0
                                    : ((double) passed /
                                    studentResults.size()) * 100
                    )
            );

            response.add(dto);
        }

        /*
         * ================================================================
         * SORT
         * ================================================================
         */

        if (topPerformers) {

            // Highest average first
            response.sort(
                    Comparator.comparing(
                            StudentPerformanceDTO::
                                    getAveragePercentage
                    ).reversed()
            );

        } else {

            // Lowest average first
            response.sort(
                    Comparator.comparing(
                            StudentPerformanceDTO::
                                    getAveragePercentage
                    )
            );
        }

        /*
         * ================================================================
         * TOP 5 / NEEDS ATTENTION 5
         * ================================================================
         */

        return response.stream()
                .limit(5)
                .collect(Collectors.toList());
    }


    /*
     * ============================================================
     * GET LOGGED-IN USER
     * ============================================================
     */
    private User getLoggedInUser(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }


    /*
     * ============================================================
     * ROUND DECIMAL
     * ============================================================
     */
    private Double round(Double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}
