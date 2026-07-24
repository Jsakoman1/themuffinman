package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.common.errors.ServiceErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessResourceAssignmentService {
    // Assignments are created inside the booking transaction and are never exposed as private identity data publicly.
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public List<Map<String, Object>> assignForBooking(Long bookingId, BusinessOffering offering, Instant startsAt, Instant endsAt) {
        List<Map<String, Object>> requirements = jdbcTemplate.queryForList(
                "select resource_pool_id, resource_type, required_count, assignment_mode from business_offering_resource_requirement where business_offering_id = ? order by id",
                offering.getId());
        List<Map<String, Object>> assignments = new ArrayList<>();
        for (Map<String, Object> requirement : requirements) {
            Long poolId = numberAsLong(requirement.get("resource_pool_id"));
            String resourceType = String.valueOf(requirement.get("resource_type"));
            int count = ((Number) requirement.get("required_count")).intValue();
            String poolCondition = poolId == null ? "resource.resource_pool_id is null" : "resource.resource_pool_id = ?";
            String sql = "select resource.id, resource.resource_key, resource.label, resource.public_label "
                    + "from business_resource resource where resource.business_profile_id = ? and resource.active = true "
                    + "and resource.resource_type = ? and " + poolCondition + " "
                    + "and not exists (select 1 from business_booking_resource_assignment assignment "
                    + "join business_booking existing on existing.id = assignment.business_booking_id "
                    + "where assignment.business_resource_id = resource.id "
                    + "and existing.status in ('PENDING_CONFIRMATION','CONFIRMED') "
                    + "and existing.starts_at < ? and existing.ends_at > ?) "
                    + "order by resource.id limit ? for update skip locked";
            List<Object> args = new ArrayList<>(List.of(offering.getBusinessProfile().getId(), resourceType));
            if (poolId != null) args.add(poolId);
            args.add(java.sql.Timestamp.from(endsAt));
            args.add(java.sql.Timestamp.from(startsAt));
            args.add(count);
            List<Map<String, Object>> available = jdbcTemplate.queryForList(sql, args.toArray());
            if (available.size() < count) {
                throw ServiceErrors.conflict("Required business resources are not available for this slot");
            }
            for (Map<String, Object> resource : available) {
                Long resourceId = numberAsLong(resource.get("id"));
                jdbcTemplate.update("insert into business_booking_resource_assignment (business_booking_id, business_resource_id, assigned_units) values (?, ?, 1)", bookingId, resourceId);
                Map<String, Object> safe = new LinkedHashMap<>();
                safe.put("resourceId", resourceId);
                safe.put("resourceKey", resource.get("resource_key"));
                safe.put("publicLabel", resource.get("public_label"));
                safe.put("assignmentMode", requirement.get("assignment_mode"));
                assignments.add(safe);
            }
        }
        return assignments;
    }

    /** Rebuilds the current assignment for a rescheduled booking in the same transaction. */
    // The assignment table is the live reservation surface; the booking snapshot remains the original quote snapshot.
    @Transactional
    public List<Map<String, Object>> reassignForBooking(Long bookingId, BusinessOffering offering, Instant startsAt, Instant endsAt) {
        jdbcTemplate.update("delete from business_booking_resource_assignment where business_booking_id = ?", bookingId);
        return assignForBooking(bookingId, offering, startsAt, endsAt);
    }

    private Long numberAsLong(Object value) { return value == null ? null : ((Number) value).longValue(); }
}
