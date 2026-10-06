package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DashboardResponseDTO {
    private long totalColleges;
    private long totalBranches;
    private long totalCourses;
    private long totalSections;
    private long myCourses;
    private long practiceQuestions;
    private long totalUnits;
    private long attemptedUnits;
    private double completionPercentage;
    private double pendingPercentage;
    private Long rank;
    private long rankedStudents;
    private String rankingScope;
    private int streakDays;
    private List<PracticeDay> practiceWeek = List.of();
    private long todayQuestions;
    private int dailyGoal;
    private OffsetDateTime refreshedAt;
    private List<LeaderboardEntry> leaderboard = List.of();
    private List<Activity> recentActivities = List.of();

    public record LeaderboardEntry(long userId, String name, long rank,
            double percentage, long examsCompleted, boolean current) {}
    public record Activity(String id, String type, String title, String detail,
            OffsetDateTime occurredAt) {}
    public record PracticeDay(LocalDate date, boolean practiced, boolean today, boolean future) {}
}
