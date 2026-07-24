package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessPricingRuleRepository extends JpaRepository<BusinessPricingRule, Long> {
    List<BusinessPricingRule> findByBusinessOfferingIdAndActiveTrueOrderBySortOrderAscIdAsc(Long offeringId);
}
