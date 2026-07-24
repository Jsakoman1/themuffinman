package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.dto.BusinessQuoteResponseDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import com.themuffinman.app.business.model.BusinessPricingRule;
import com.themuffinman.app.business.repository.BusinessPricingRuleRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessPricingCalculationService {

    private final BusinessPricingRuleRepository pricingRuleRepository;

    public BusinessPricingCalculationService(BusinessPricingRuleRepository pricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
    }

    /** Kept for focused pure-calculation tests that do not need persisted rules. */
    public BusinessPricingCalculationService() {
        this.pricingRuleRepository = null;
    }

    public BusinessQuoteResponseDTO calculate(BusinessOffering offering, BusinessQuoteRequestDTO request) {
        BusinessOfferingPricingType pricingType = offering.getPricingType();
        BigDecimal quantity = request.getQuantity() == null ? BigDecimal.ONE : request.getQuantity();
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw ServiceErrors.badRequest("Quantity must be greater than zero");
        }
        Integer duration = request.getDurationMinutes() != null
                ? request.getDurationMinutes()
                : offering.getDefaultDurationMinutes();
        if (duration != null && duration < 1) {
            throw ServiceErrors.badRequest("Duration must be at least one minute");
        }

        List<String> explanations = new ArrayList<>();
        BigDecimal amount;
        String state;
        boolean ownerReview = pricingType == BusinessOfferingPricingType.CUSTOM_QUOTE;
        if (pricingType == BusinessOfferingPricingType.FREE) {
            amount = BigDecimal.ZERO.setScale(2);
            state = "CALCULATED";
            explanations.add("This offering is free.");
        } else if (pricingType == BusinessOfferingPricingType.CUSTOM_QUOTE) {
            amount = null;
            state = "QUOTE_REQUIRED";
            explanations.add("The business must review and price this request.");
        } else {
            if (offering.getBasePriceAmount() == null || offering.getBasePriceCurrency() == null) {
                throw ServiceErrors.badRequest("This offering has incomplete pricing configuration");
            }
            amount = offering.getBasePriceAmount().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            state = pricingType == BusinessOfferingPricingType.FROM ? "FROM" : "CALCULATED";
            explanations.add("Base price " + offering.getBasePriceAmount() + " " + offering.getBasePriceCurrency());
            if (quantity.compareTo(BigDecimal.ONE) != 0) {
                explanations.add("Quantity " + quantity + " applied.");
            }
            List<BusinessPricingRule> rules = pricingRuleRepository == null ? List.of()
                    : pricingRuleRepository.findByBusinessOfferingIdAndActiveTrueOrderBySortOrderAscIdAsc(offering.getId());
            for (BusinessPricingRule rule : rules) {
                if (!matches(rule, quantity)) continue;
                BigDecimal factor = billingFactor(rule.getBillingUnit(), quantity, duration);
                BigDecimal line = rule.getAmount() == null ? BigDecimal.ZERO : rule.getAmount().multiply(factor);
                String ruleType = rule.getRuleType() == null ? "ADD_ON" : rule.getRuleType().toUpperCase();
                if ("MODIFIER".equals(ruleType) && rule.getModifier() != null) {
                    line = amount.multiply(rule.getModifier()).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                }
                if (!"BASE".equals(ruleType) && !"MINIMUM_CHARGE".equals(ruleType)) {
                    amount = amount.add(line);
                }
                if ("MINIMUM_CHARGE".equals(ruleType) && amount.compareTo(line) < 0) {
                    amount = line;
                }
                explanations.add("Pricing rule " + rule.getRuleKey() + " applied.");
            }
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        }

        return BusinessQuoteResponseDTO.builder()
                .businessOfferingId(offering.getId())
                .offeringTitle(offering.getTitle())
                .pricingState(state)
                .totalAmount(amount)
                .currency(offering.getBasePriceCurrency())
                .quantity(quantity)
                .durationMinutes(duration)
                .startsAt(request.getStartsAt())
                .endsAt(request.getStartsAt() != null && duration != null
                        ? request.getStartsAt().plus(Duration.ofMinutes(duration)) : null)
                .timezone(offering.getBusinessProfile().getTimezone())
                .ownerReviewRequired(ownerReview)
                .schemaVersion(offering.getSchemaVersion())
                .explanations(explanations)
                .build();
    }

    private boolean matches(BusinessPricingRule rule, BigDecimal quantity) {
        return (rule.getQuantityFrom() == null || quantity.compareTo(rule.getQuantityFrom()) >= 0)
                && (rule.getQuantityTo() == null || quantity.compareTo(rule.getQuantityTo()) <= 0);
    }

    private BigDecimal billingFactor(String billingUnit, BigDecimal quantity, Integer duration) {
        if (billingUnit == null) return quantity;
        return switch (billingUnit.toUpperCase()) {
            case "BOOKING", "PACKAGE", "ITEM", "QUANTITY", "PARTICIPANT" -> quantity;
            case "MINUTE" -> BigDecimal.valueOf(duration == null ? 0 : duration);
            case "HOUR" -> BigDecimal.valueOf(duration == null ? 0 : duration)
                    .divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
            case "DAY" -> BigDecimal.valueOf(duration == null ? 0 : duration)
                    .divide(BigDecimal.valueOf(1440), 6, RoundingMode.HALF_UP);
            default -> BigDecimal.ONE;
        };
    }
}
