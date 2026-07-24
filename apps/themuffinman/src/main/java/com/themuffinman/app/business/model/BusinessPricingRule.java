package com.themuffinman.app.business.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "business_pricing_rule")
public class BusinessPricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_offering_id", nullable = false)
    private BusinessOffering businessOffering;

    @Column(name = "rule_key", nullable = false, length = 80)
    private String ruleKey;

    @Column(name = "rule_type", nullable = false, length = 40)
    private String ruleType;

    @Column(name = "billing_unit", nullable = false, length = 40)
    private String billingUnit;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Column(name = "quantity_from", precision = 12, scale = 3)
    private BigDecimal quantityFrom;

    @Column(name = "quantity_to", precision = 12, scale = 3)
    private BigDecimal quantityTo;

    @Column(precision = 12, scale = 2)
    private BigDecimal modifier;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
