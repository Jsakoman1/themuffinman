package com.themuffinman.app.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.business.dto.BusinessResourceConfigurationDTO;
import com.themuffinman.app.business.dto.BusinessResourcePoolRequestDTO;
import com.themuffinman.app.business.dto.BusinessResourcePoolResponseDTO;
import com.themuffinman.app.business.dto.BusinessResourceRequestDTO;
import com.themuffinman.app.business.dto.BusinessResourceResponseDTO;
import com.themuffinman.app.business.dto.BusinessResourceRequirementRequestDTO;
import com.themuffinman.app.business.dto.BusinessResourceRequirementResponseDTO;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessResourceService {
    private static final TypeReference<Map<String, Object>> METADATA_TYPE = new TypeReference<>() {};

    private final BusinessProfileRepository profileRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public BusinessResourceConfigurationDTO getOwnerResourceConfiguration(Long profileId, AppUser owner) {
        requireOwner(profileId, owner);
        List<BusinessResourcePoolResponseDTO> pools = jdbcTemplate.query(
                "select id, pool_key, label, resource_type, capacity, public_label, active from business_resource_pool where business_profile_id = ? order by id",
                (rs, rowNum) -> new BusinessResourcePoolResponseDTO(
                        rs.getLong("id"), rs.getString("pool_key"), rs.getString("label"), rs.getString("resource_type"),
                        rs.getInt("capacity"), rs.getString("public_label"), rs.getBoolean("active")), profileId);
        List<BusinessResourceResponseDTO> resources = jdbcTemplate.query(
                "select id, resource_pool_id, resource_key, label, resource_type, public_label, active, metadata from business_resource where business_profile_id = ? order by id",
                (rs, rowNum) -> new BusinessResourceResponseDTO(
                        rs.getLong("id"), nullableLong(rs.getObject("resource_pool_id")), rs.getString("resource_key"),
                        rs.getString("label"), rs.getString("resource_type"), rs.getString("public_label"),
                        rs.getBoolean("active"), metadata(rs.getObject("metadata"))), profileId);
        List<BusinessResourceRequirementResponseDTO> requirements = jdbcTemplate.query(
                "select requirement.id, requirement.business_offering_id, offering.title as offering_title, requirement.resource_pool_id, requirement.resource_type, requirement.required_count, requirement.assignment_mode from business_offering_resource_requirement requirement join business_offering offering on offering.id = requirement.business_offering_id where offering.business_profile_id = ? order by requirement.business_offering_id, requirement.id",
                (rs, rowNum) -> new BusinessResourceRequirementResponseDTO(
                        rs.getLong("id"), rs.getLong("business_offering_id"), rs.getString("offering_title"),
                        nullableLong(rs.getObject("resource_pool_id")), rs.getString("resource_type"),
                        rs.getInt("required_count"), rs.getString("assignment_mode")), profileId);
        return new BusinessResourceConfigurationDTO(profileId, pools, resources, requirements);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOwnerResources(Long profileId, AppUser owner) {
        BusinessResourceConfigurationDTO configuration = getOwnerResourceConfiguration(profileId, owner);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("businessProfileId", configuration.businessProfileId());
        result.put("pools", configuration.pools());
        result.put("resources", configuration.resources());
        result.put("requirements", configuration.requirements());
        return result;
    }

    @Transactional
    public BusinessResourceConfigurationDTO createPool(Long profileId, BusinessResourcePoolRequestDTO request, AppUser owner) {
        requireOwner(profileId, owner);
        validatePool(request);
        jdbcTemplate.update("insert into business_resource_pool (business_profile_id, pool_key, label, resource_type, capacity, public_label, active) values (?, ?, ?, ?, ?, ?, ?)",
                profileId, required(request.getPoolKey(), "poolKey"), required(request.getLabel(), "label"),
                required(request.getResourceType(), "resourceType"), positive(request.getCapacity(), "capacity"),
                trimToNull(request.getPublicLabel()), defaultTrue(request.getActive()));
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO updatePool(Long profileId, Long poolId, BusinessResourcePoolRequestDTO request, AppUser owner) {
        requireOwner(profileId, owner);
        validatePool(request);
        int changed = jdbcTemplate.update("update business_resource_pool set pool_key = ?, label = ?, resource_type = ?, capacity = ?, public_label = ?, active = ?, updated_at = now() where id = ? and business_profile_id = ?",
                required(request.getPoolKey(), "poolKey"), required(request.getLabel(), "label"), required(request.getResourceType(), "resourceType"),
                positive(request.getCapacity(), "capacity"), trimToNull(request.getPublicLabel()), defaultTrue(request.getActive()), poolId, profileId);
        requireChanged(changed, "Resource pool not found");
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO deletePool(Long profileId, Long poolId, AppUser owner) {
        requireOwner(profileId, owner);
        requireChanged(jdbcTemplate.update("delete from business_resource_pool where id = ? and business_profile_id = ?", poolId, profileId), "Resource pool not found");
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO createResource(Long profileId, BusinessResourceRequestDTO request, AppUser owner) {
        requireOwner(profileId, owner);
        validateResource(profileId, request);
        jdbcTemplate.update("insert into business_resource (business_profile_id, resource_pool_id, resource_key, label, resource_type, public_label, active, metadata) values (?, ?, ?, ?, ?, ?, ?, cast(? as jsonb))",
                profileId, request.getResourcePoolId(), required(request.getResourceKey(), "resourceKey"), required(request.getLabel(), "label"),
                required(request.getResourceType(), "resourceType"), trimToNull(request.getPublicLabel()), defaultTrue(request.getActive()), metadataJson(request.getMetadata()));
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO updateResource(Long profileId, Long resourceId, BusinessResourceRequestDTO request, AppUser owner) {
        requireOwner(profileId, owner);
        validateResource(profileId, request);
        int changed = jdbcTemplate.update("update business_resource set resource_pool_id = ?, resource_key = ?, label = ?, resource_type = ?, public_label = ?, active = ?, metadata = cast(? as jsonb), updated_at = now() where id = ? and business_profile_id = ?",
                request.getResourcePoolId(), required(request.getResourceKey(), "resourceKey"), required(request.getLabel(), "label"),
                required(request.getResourceType(), "resourceType"), trimToNull(request.getPublicLabel()), defaultTrue(request.getActive()), metadataJson(request.getMetadata()), resourceId, profileId);
        requireChanged(changed, "Resource not found");
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO deleteResource(Long profileId, Long resourceId, AppUser owner) {
        requireOwner(profileId, owner);
        try {
            requireChanged(jdbcTemplate.update("delete from business_resource where id = ? and business_profile_id = ?", resourceId, profileId), "Resource not found");
        } catch (DataIntegrityViolationException exception) {
            throw ServiceErrors.conflict("Resource is retained by an existing booking and cannot be deleted");
        }
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO createRequirement(Long profileId, BusinessResourceRequirementRequestDTO request, AppUser owner) {
        requireOwner(profileId, owner);
        validateRequirement(profileId, request);
        jdbcTemplate.update("insert into business_offering_resource_requirement (business_offering_id, resource_pool_id, resource_type, required_count, assignment_mode) values (?, ?, ?, ?, ?)",
                request.getBusinessOfferingId(), request.getResourcePoolId(), required(request.getResourceType(), "resourceType"),
                positive(request.getRequiredCount(), "requiredCount"), required(request.getAssignmentMode(), "assignmentMode"));
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO updateRequirement(Long profileId, Long requirementId, BusinessResourceRequirementRequestDTO request, AppUser owner) {
        requireOwner(profileId, owner);
        validateRequirement(profileId, request);
        int changed = jdbcTemplate.update("update business_offering_resource_requirement set business_offering_id = ?, resource_pool_id = ?, resource_type = ?, required_count = ?, assignment_mode = ? where id = ? and business_offering_id in (select id from business_offering where business_profile_id = ?)",
                request.getBusinessOfferingId(), request.getResourcePoolId(), required(request.getResourceType(), "resourceType"),
                positive(request.getRequiredCount(), "requiredCount"), required(request.getAssignmentMode(), "assignmentMode"), requirementId, profileId);
        requireChanged(changed, "Resource requirement not found");
        return getOwnerResourceConfiguration(profileId, owner);
    }

    @Transactional
    public BusinessResourceConfigurationDTO deleteRequirement(Long profileId, Long requirementId, AppUser owner) {
        requireOwner(profileId, owner);
        requireChanged(jdbcTemplate.update("delete from business_offering_resource_requirement where id = ? and business_offering_id in (select id from business_offering where business_profile_id = ?)", requirementId, profileId), "Resource requirement not found");
        return getOwnerResourceConfiguration(profileId, owner);
    }

    private void validatePool(BusinessResourcePoolRequestDTO request) {
        if (request == null) throw ServiceErrors.badRequest("Resource pool is required");
        positive(request.getCapacity(), "capacity");
    }

    private void validateResource(Long profileId, BusinessResourceRequestDTO request) {
        if (request == null) throw ServiceErrors.badRequest("Resource is required");
        requireOwnedPool(profileId, request.getResourcePoolId());
    }

    private void validateRequirement(Long profileId, BusinessResourceRequirementRequestDTO request) {
        if (request == null) throw ServiceErrors.badRequest("Resource requirement is required");
        if (request.getBusinessOfferingId() == null || count("select count(*) from business_offering where id = ? and business_profile_id = ?", request.getBusinessOfferingId(), profileId) == 0) {
            throw ServiceErrors.badRequest("Offering does not belong to this business");
        }
        requireOwnedPool(profileId, request.getResourcePoolId());
        positive(request.getRequiredCount(), "requiredCount");
    }

    private void requireOwnedPool(Long profileId, Long poolId) {
        if (poolId != null && count("select count(*) from business_resource_pool where id = ? and business_profile_id = ?", poolId, profileId) == 0) {
            throw ServiceErrors.badRequest("Resource pool does not belong to this business");
        }
    }

    private int count(String sql, Object... arguments) {
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, arguments);
        return result == null ? 0 : result;
    }

    private void requireOwner(Long profileId, AppUser owner) {
        if (owner == null) throw ServiceErrors.notFound("Business profile not found");
        profileRepository.findById(profileId).filter(profile -> profile.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
    }

    private void requireChanged(int changed, String message) {
        if (changed == 0) throw ServiceErrors.notFound(message);
    }

    private String required(String value, String key) {
        String normalized = trimToNull(value);
        if (normalized == null) throw ServiceErrors.badRequest(key + " is required");
        return normalized;
    }

    private int positive(Integer value, String key) {
        if (value == null || value < 1) throw ServiceErrors.badRequest(key + " must be at least one");
        return value;
    }

    private boolean defaultTrue(Boolean value) { return value == null || value; }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String metadataJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException exception) {
            throw ServiceErrors.badRequest("Resource metadata must be valid JSON");
        }
    }

    private Map<String, Object> metadata(Object value) {
        if (value == null) return Map.of();
        try {
            return objectMapper.readValue(String.valueOf(value), METADATA_TYPE);
        } catch (JsonProcessingException exception) {
            return new LinkedHashMap<>();
        }
    }

    private Long nullableLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
