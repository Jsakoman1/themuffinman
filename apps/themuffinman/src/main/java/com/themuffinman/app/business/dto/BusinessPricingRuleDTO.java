package com.themuffinman.app.business.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BusinessPricingRuleDTO {
    private Long id;
    private String ruleKey;
    private String ruleType;
    private String billingUnit;
    private BigDecimal amount;
    private String currency;
    private BigDecimal quantityFrom;
    private BigDecimal quantityTo;
    private BigDecimal modifier;
    private Object conditions;
    private int sortOrder;
}
