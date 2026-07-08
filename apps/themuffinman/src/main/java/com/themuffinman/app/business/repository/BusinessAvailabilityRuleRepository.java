package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessAvailabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessAvailabilityRuleRepository extends JpaRepository<BusinessAvailabilityRule, Long> {

    @Query("""
            select rule
            from BusinessAvailabilityRule rule
            join fetch rule.businessProfile profile
            join fetch profile.owner owner
            left join fetch rule.businessOffering offering
            where owner.id = :ownerId
            order by rule.dayOfWeek asc, rule.startTimeLocal asc, rule.id asc
            """)
    List<BusinessAvailabilityRule> findByOwnerId(Long ownerId);

    @Query("""
            select rule
            from BusinessAvailabilityRule rule
            join fetch rule.businessProfile profile
            join fetch profile.owner owner
            left join fetch rule.businessOffering offering
            where rule.id = :ruleId and owner.id = :ownerId
            """)
    Optional<BusinessAvailabilityRule> findOwnedById(Long ruleId, Long ownerId);

    @Query("""
            select rule
            from BusinessAvailabilityRule rule
            join fetch rule.businessProfile profile
            left join fetch rule.businessOffering offering
            where profile.id = :businessProfileId
            and rule.active = true
            order by rule.dayOfWeek asc, rule.startTimeLocal asc, rule.id asc
            """)
    List<BusinessAvailabilityRule> findActiveByBusinessProfileId(Long businessProfileId);

    @Query("""
            select rule
            from BusinessAvailabilityRule rule
            join fetch rule.businessProfile profile
            left join fetch rule.businessOffering offering
            where profile.slug = :slug
            and profile.active = true
            and rule.active = true
            order by rule.dayOfWeek asc, rule.startTimeLocal asc, rule.id asc
            """)
    List<BusinessAvailabilityRule> findActiveByBusinessSlug(String slug);
}
