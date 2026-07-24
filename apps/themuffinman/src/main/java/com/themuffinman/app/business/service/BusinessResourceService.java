package com.themuffinman.app.business.service;

import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessResourceService {
    private final BusinessProfileRepository profileRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public Map<String, Object> getOwnerResources(Long profileId, AppUser owner) {
        requireOwner(profileId, owner);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("businessProfileId", profileId);
        result.put("pools", jdbcTemplate.queryForList("select id, pool_key, label, resource_type, capacity, public_label, active from business_resource_pool where business_profile_id = ? order by id", profileId));
        result.put("resources", jdbcTemplate.queryForList("select id, resource_pool_id, resource_key, label, resource_type, public_label, active, metadata from business_resource where business_profile_id = ? order by id", profileId));
        result.put("requirements", jdbcTemplate.queryForList("select requirement.id, requirement.business_offering_id, offering.title as offering_title, requirement.resource_pool_id, requirement.resource_type, requirement.required_count, requirement.assignment_mode from business_offering_resource_requirement requirement join business_offering offering on offering.id = requirement.business_offering_id where offering.business_profile_id = ? order by requirement.business_offering_id, requirement.id", profileId));
        return result;
    }

    @Transactional
    public Map<String, Object> createPool(Long profileId, Map<String, Object> request, AppUser owner) {
        requireOwner(profileId, owner);
        if (request == null) throw ServiceErrors.badRequest("Resource pool is required");
        jdbcTemplate.update("insert into business_resource_pool (business_profile_id, pool_key, label, resource_type, capacity, public_label, active) values (?, ?, ?, ?, ?, ?, ?)", profileId, required(request, "poolKey"), required(request, "label"), required(request, "resourceType"), number(request, "capacity", 1), text(request, "publicLabel"), bool(request, "active", true));
        return getOwnerResources(profileId, owner);
    }

    @Transactional
    public Map<String, Object> createResource(Long profileId, Map<String, Object> request, AppUser owner) {
        requireOwner(profileId, owner);
        if (request == null) throw ServiceErrors.badRequest("Resource is required");
        Long poolId = longValue(request, "resourcePoolId");
        if (poolId != null && jdbcTemplate.queryForObject(
                "select count(*) from business_resource_pool where id = ? and business_profile_id = ?", Integer.class, poolId, profileId) == 0) {
            throw ServiceErrors.badRequest("Resource pool does not belong to this business");
        }
        jdbcTemplate.update("insert into business_resource (business_profile_id, resource_pool_id, resource_key, label, resource_type, public_label, active, metadata) values (?, ?, ?, ?, ?, ?, ?, cast(? as jsonb))",
                profileId, poolId, required(request, "resourceKey"), required(request, "label"), required(request, "resourceType"), text(request, "publicLabel"), bool(request, "active", true), textOrDefault(request, "metadata", "{}"));
        return getOwnerResources(profileId, owner);
    }

    @Transactional
    public Map<String, Object> createRequirement(Long profileId, Map<String, Object> request, AppUser owner) {
        requireOwner(profileId, owner);
        if (request == null) throw ServiceErrors.badRequest("Resource requirement is required");
        Long offeringId = longValue(request, "businessOfferingId");
        if (offeringId == null || jdbcTemplate.queryForObject("select count(*) from business_offering where id = ? and business_profile_id = ?", Integer.class, offeringId, profileId) == 0) {
            throw ServiceErrors.badRequest("Offering does not belong to this business");
        }
        Long poolId = longValue(request, "resourcePoolId");
        if (poolId != null && jdbcTemplate.queryForObject("select count(*) from business_resource_pool where id = ? and business_profile_id = ?", Integer.class, poolId, profileId) == 0) {
            throw ServiceErrors.badRequest("Resource pool does not belong to this business");
        }
        int requiredCount = number(request, "requiredCount", 1);
        if (requiredCount < 1) throw ServiceErrors.badRequest("Required resource count must be at least one");
        jdbcTemplate.update("insert into business_offering_resource_requirement (business_offering_id, resource_pool_id, resource_type, required_count, assignment_mode) values (?, ?, ?, ?, ?)", offeringId, poolId, required(request, "resourceType"), requiredCount, textOrDefault(request, "assignmentMode", "ANY_AVAILABLE"));
        return getOwnerResources(profileId, owner);
    }

    private void requireOwner(Long profileId, AppUser owner) { profileRepository.findById(profileId).filter(profile -> profile.getOwner().getId().equals(owner.getId())).orElseThrow(() -> ServiceErrors.notFound("Business profile not found")); }
    private String required(Map<String, Object> map, String key) { String value = text(map, key); if (value == null || value.isBlank()) throw ServiceErrors.badRequest(key + " is required"); return value.trim(); }
    private String text(Map<String, Object> map, String key) { return map.get(key) == null ? null : String.valueOf(map.get(key)); }
    private int number(Map<String, Object> map, String key, int fallback) { return map.get(key) == null ? fallback : Integer.parseInt(String.valueOf(map.get(key))); }
    private Long longValue(Map<String, Object> map, String key) { return map.get(key) == null ? null : Long.valueOf(String.valueOf(map.get(key))); }
    private String textOrDefault(Map<String, Object> map, String key, String fallback) { return text(map, key) == null ? fallback : text(map, key); }
    private boolean bool(Map<String, Object> map, String key, boolean fallback) { return map.get(key) == null ? fallback : Boolean.TRUE.equals(map.get(key)); }
}
