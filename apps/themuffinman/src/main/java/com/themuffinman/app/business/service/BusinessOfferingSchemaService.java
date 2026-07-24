package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.dto.BusinessServiceSchemaDTO;
import com.themuffinman.app.business.dto.BusinessDemandFieldDTO;
import com.themuffinman.app.business.dto.BusinessOfferingOptionDTO;
import com.themuffinman.app.business.dto.BusinessPricingRuleDTO;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusinessOfferingSchemaService {
    private static final Set<String> SUPPORTED_VALUE_TYPES = Set.of("TEXT", "LONG_TEXT", "INTEGER", "DECIMAL", "BOOLEAN", "DATE", "TIME", "DATETIME", "ENUM", "JSON");
    private final BusinessOfferingRepository offeringRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public Map<String, Object> getOwnerSchema(Long offeringId, AppUser owner) {
        requireOwnedOffering(offeringId, owner);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("offeringId", offeringId);
        result.put("demandFields", jdbcTemplate.queryForList("select id, field_key, label, description, value_type, required, customer_visible, owner_visible, retention_days, validation_schema, active, sort_order from business_demand_field where business_offering_id = ? order by sort_order, id", offeringId));
        result.put("options", jdbcTemplate.queryForList("select id, option_key, label, description, value_type, required, active, sort_order from business_offering_option where business_offering_id = ? order by sort_order, id", offeringId));
        result.put("pricingRules", jdbcTemplate.queryForList("select id, rule_key, rule_type, billing_unit, amount, currency, quantity_from, quantity_to, modifier, conditions, active, sort_order from business_pricing_rule where business_offering_id = ? order by sort_order, id", offeringId));
        return result;
    }

    @Transactional(readOnly = true)
    public BusinessServiceSchemaDTO getPublicSchema(String slug, Long offeringId) {
        BusinessOffering offering = offeringRepository.findById(offeringId)
                .filter(item -> item.isActive() && item.getBusinessProfile().isActive()
                        && item.getBusinessProfile().isBookingEnabled()
                        && item.getBusinessProfile().getSlug().equals(slug))
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
        return BusinessServiceSchemaDTO.builder()
                .businessOfferingId(offering.getId())
                .schemaVersion(offering.getSchemaVersion())
                .demandFields(demandFields(offeringId))
                .options(options(offeringId))
                .pricingRules(pricingRules(offeringId))
                .build();
    }

    private List<Map<String, Object>> rows(String sql, Long offeringId) { return jdbcTemplate.queryForList(sql, offeringId); }

    private List<BusinessDemandFieldDTO> demandFields(Long offeringId) {
        return rows("select id, field_key, label, description, value_type, required, customer_visible, retention_days, validation_schema, sort_order from business_demand_field where business_offering_id = ? and active = true order by sort_order, id", offeringId)
                .stream().map(row -> BusinessDemandFieldDTO.builder()
                        .id(longValue(row, "id")).fieldKey(string(row, "field_key")).label(string(row, "label"))
                        .description(string(row, "description")).valueType(string(row, "value_type"))
                        .required(boolValue(row, "required")).customerVisible(boolValue(row, "customer_visible"))
                        .retentionDays(integerValue(row, "retention_days")).validationSchema(row.get("validation_schema"))
                        .sortOrder(integerValue(row, "sort_order") == null ? 0 : integerValue(row, "sort_order")).build()).toList();
    }

    private List<BusinessOfferingOptionDTO> options(Long offeringId) {
        return rows("select id, option_key, label, description, value_type, required, sort_order from business_offering_option where business_offering_id = ? and active = true order by sort_order, id", offeringId)
                .stream().map(row -> BusinessOfferingOptionDTO.builder().id(longValue(row, "id"))
                        .optionKey(string(row, "option_key")).label(string(row, "label")).description(string(row, "description"))
                        .valueType(string(row, "value_type")).required(boolValue(row, "required"))
                        .sortOrder(integerValue(row, "sort_order") == null ? 0 : integerValue(row, "sort_order")).build()).toList();
    }

    private List<BusinessPricingRuleDTO> pricingRules(Long offeringId) {
        return rows("select id, rule_key, rule_type, billing_unit, amount, currency, quantity_from, quantity_to, modifier, conditions, sort_order from business_pricing_rule where business_offering_id = ? and active = true order by sort_order, id", offeringId)
                .stream().map(row -> BusinessPricingRuleDTO.builder().id(longValue(row, "id"))
                        .ruleKey(string(row, "rule_key")).ruleType(string(row, "rule_type")).billingUnit(string(row, "billing_unit"))
                        .amount(decimal(row, "amount")).currency(string(row, "currency")).quantityFrom(decimal(row, "quantity_from"))
                        .quantityTo(decimal(row, "quantity_to")).modifier(decimal(row, "modifier")).conditions(row.get("conditions"))
                        .sortOrder(integerValue(row, "sort_order") == null ? 0 : integerValue(row, "sort_order")).build()).toList();
    }

    @Transactional
    public Map<String, Object> replaceOwnerSchema(Long offeringId, Map<String, Object> request, AppUser owner) {
        BusinessOffering offering = requireOwnedOffering(offeringId, owner);
        if (request == null) throw ServiceErrors.badRequest("Offering schema is required");
        jdbcTemplate.update("delete from business_demand_field where business_offering_id = ?", offeringId);
        jdbcTemplate.update("delete from business_offering_option where business_offering_id = ?", offeringId);
        jdbcTemplate.update("delete from business_pricing_rule where business_offering_id = ?", offeringId);
        insertDemandFields(offeringId, list(request.get("demandFields")), true);
        insertOptions(offeringId, list(request.get("options")));
        insertPricingRules(offeringId, list(request.get("pricingRules")));
        offering.setSchemaVersion(offering.getSchemaVersion() + 1);
        offeringRepository.save(offering);
        return getOwnerSchema(offeringId, owner);
    }

    @Transactional(readOnly = true)
    public void validateCustomerInput(Long offeringId, Map<String, String> answers, Map<String, String> selectedOptions) {
        Map<String, String> safeAnswers = answers == null ? Map.of() : answers;
        Map<String, String> safeOptions = selectedOptions == null ? Map.of() : selectedOptions;
        for (Map<String, Object> field : jdbcTemplate.queryForList(
                "select field_key, label from business_demand_field where business_offering_id = ? and active = true and required = true and customer_visible = true", offeringId)) {
            String key = String.valueOf(field.get("field_key"));
            if (safeAnswers.get(key) == null || safeAnswers.get(key).isBlank()) {
                throw ServiceErrors.badRequest("Required information missing: " + field.get("label"));
            }
        }
        for (Map<String, Object> option : jdbcTemplate.queryForList(
                "select option_key, label from business_offering_option where business_offering_id = ? and active = true and required = true", offeringId)) {
            String key = String.valueOf(option.get("option_key"));
            if (safeOptions.get(key) == null || safeOptions.get(key).isBlank()) {
                throw ServiceErrors.badRequest("Required option missing: " + option.get("label"));
            }
        }
        safeAnswers.forEach((key, value) -> validateText(key, value, "demand answer"));
        safeOptions.forEach((key, value) -> validateText(key, value, "selected option"));
    }

    private void validateText(String key, String value, String label) {
        if (key == null || key.isBlank() || key.length() > 80 || value == null || value.length() > 2000) {
            throw ServiceErrors.badRequest("Invalid " + label);
        }
    }

    private void insertDemandFields(Long offeringId, List<Map<String, Object>> fields, boolean customerVisibleDefault) {
        for (Map<String, Object> field : fields) {
            String key = requiredText(field, "fieldKey");
            String label = requiredText(field, "label");
            String valueType = textOrDefault(field, "valueType", "TEXT").toUpperCase();
            if (!SUPPORTED_VALUE_TYPES.contains(valueType)) throw ServiceErrors.badRequest("Unsupported demand value type: " + valueType);
            jdbcTemplate.update("insert into business_demand_field (business_offering_id, field_key, label, description, value_type, required, customer_visible, owner_visible, retention_days, validation_schema, active, sort_order) values (?, ?, ?, ?, ?, ?, ?, ?, ?, cast(? as jsonb), ?, ?)", offeringId, key, label, text(field, "description"), valueType, bool(field, "required"), boolOrDefault(field, "customerVisible", customerVisibleDefault), boolOrDefault(field, "ownerVisible", true), integer(field, "retentionDays"), textOrDefault(field, "validationSchema", "{}"), boolOrDefault(field, "active", true), integerOrZero(field, "sortOrder"));
        }
    }

    private void insertOptions(Long offeringId, List<Map<String, Object>> options) {
        for (Map<String, Object> option : options) {
            jdbcTemplate.update("insert into business_offering_option (business_offering_id, option_key, label, description, value_type, required, active, sort_order) values (?, ?, ?, ?, ?, ?, ?, ?)", offeringId, requiredText(option, "optionKey"), requiredText(option, "label"), text(option, "description"), textOrDefault(option, "valueType", "TEXT"), bool(option, "required"), boolOrDefault(option, "active", true), integerOrZero(option, "sortOrder"));
        }
    }

    private void insertPricingRules(Long offeringId, List<Map<String, Object>> rules) {
        for (Map<String, Object> rule : rules) {
            jdbcTemplate.update("insert into business_pricing_rule (business_offering_id, rule_key, rule_type, billing_unit, amount, currency, quantity_from, quantity_to, modifier, conditions, active, sort_order) values (?, ?, ?, ?, ?, ?, ?, ?, ?, cast(? as jsonb), ?, ?)", offeringId, requiredText(rule, "ruleKey"), textOrDefault(rule, "ruleType", "BASE"), textOrDefault(rule, "billingUnit", "BOOKING"), number(rule, "amount"), text(rule, "currency"), number(rule, "quantityFrom"), number(rule, "quantityTo"), number(rule, "modifier"), textOrDefault(rule, "conditions", "{}"), boolOrDefault(rule, "active", true), integerOrZero(rule, "sortOrder"));
        }
    }

    private BusinessOffering requireOwnedOffering(Long id, AppUser owner) {
        return offeringRepository.findOwnedById(id, owner.getId()).orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
    }
    @SuppressWarnings("unchecked") private List<Map<String, Object>> list(Object value) { return value instanceof List<?> list ? (List<Map<String, Object>>) list : new ArrayList<>(); }
    private String requiredText(Map<String, Object> map, String key) { String value = text(map, key); if (value == null || value.isBlank()) throw ServiceErrors.badRequest(key + " is required"); return value.trim(); }
    private String text(Map<String, Object> map, String key) { return map.get(key) == null ? null : String.valueOf(map.get(key)); }
    private String textOrDefault(Map<String, Object> map, String key, String fallback) { return text(map, key) == null ? fallback : text(map, key); }
    private boolean bool(Map<String, Object> map, String key) { return Boolean.TRUE.equals(map.get(key)); }
    private boolean boolOrDefault(Map<String, Object> map, String key, boolean fallback) { return map.get(key) == null ? fallback : bool(map, key); }
    private Integer integer(Map<String, Object> map, String key) { return map.get(key) == null ? null : Integer.valueOf(String.valueOf(map.get(key))); }
    private int integerOrZero(Map<String, Object> map, String key) { Integer value = integer(map, key); return value == null ? 0 : value; }
    private java.math.BigDecimal number(Map<String, Object> map, String key) { return map.get(key) == null ? null : new java.math.BigDecimal(String.valueOf(map.get(key))); }
    private String string(Map<String, Object> row, String key) { return row.get(key) == null ? null : String.valueOf(row.get(key)); }
    private Long longValue(Map<String, Object> row, String key) { return row.get(key) == null ? null : Long.valueOf(String.valueOf(row.get(key))); }
    private Integer integerValue(Map<String, Object> row, String key) { return row.get(key) == null ? null : Integer.valueOf(String.valueOf(row.get(key))); }
    private boolean boolValue(Map<String, Object> row, String key) { return Boolean.TRUE.equals(row.get(key)); }
    private java.math.BigDecimal decimal(Map<String, Object> row, String key) { return row.get(key) == null ? null : new java.math.BigDecimal(String.valueOf(row.get(key))); }
}
