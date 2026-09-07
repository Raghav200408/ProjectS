package com.project.ProjectS.repository;

import com.project.ProjectS.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByActiveTrueOrderByNameAsc();
    Optional<SubscriptionPlan> findFirstByFreeTrialTrueAndActiveTrue();
}
