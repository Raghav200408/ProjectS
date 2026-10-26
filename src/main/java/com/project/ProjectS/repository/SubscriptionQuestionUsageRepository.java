package com.project.ProjectS.repository;
import com.project.ProjectS.entity.SubscriptionQuestionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SubscriptionQuestionUsageRepository extends JpaRepository<SubscriptionQuestionUsage, Long> {
    boolean existsBySubscription_SubscriptionIdAndQuestion_QuestionId(Long subscriptionId, Long questionId);
}
