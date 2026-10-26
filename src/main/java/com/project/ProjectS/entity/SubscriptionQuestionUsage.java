package com.project.ProjectS.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "subscription_question_usage", uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "question_id"}))
@Getter @Setter
public class SubscriptionQuestionUsage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "subscription_id", nullable = false) private UserSubscription subscription;
    @ManyToOne(optional = false) @JoinColumn(name = "question_id", nullable = false) private Question question;
}
