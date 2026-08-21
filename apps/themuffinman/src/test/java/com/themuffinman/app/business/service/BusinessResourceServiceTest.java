package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessResourcePoolRequestDTO;
import com.themuffinman.app.business.dto.BusinessResourceRequestDTO;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessResourceServiceTest {
    @Mock private BusinessProfileRepository profileRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    private BusinessResourceService service;
    private AppUser owner;

    @BeforeEach
    void setUp() {
        service = new BusinessResourceService(profileRepository, jdbcTemplate);
        owner = new AppUser();
        owner.setId(7L);
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setOwner(owner);
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void createPoolReturnsTypedConfigurationAndNormalizesText() {
        when(jdbcTemplate.update(contains("insert into business_resource_pool"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(10L))).thenReturn(List.of());
        BusinessResourcePoolRequestDTO request = poolRequest();

        var result = service.createPool(10L, request, owner);

        assertEquals(10L, result.businessProfileId());
        assertEquals(List.of(), result.pools());
        verify(jdbcTemplate).update(contains("insert into business_resource_pool"),
                eq(10L), eq("chairs"), eq("Chairs"), eq("EQUIPMENT"), eq(4), eq("Chair"), eq(true));
    }

    @Test
    void updatePoolRejectsAnUnknownOwnerScopedPool() {
        when(jdbcTemplate.update(contains("update business_resource_pool"), any(Object[].class))).thenReturn(0);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.updatePool(10L, 99L, poolRequest(), owner));

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void createResourceRejectsPoolFromAnotherBusiness() {
        when(jdbcTemplate.queryForObject(contains("business_resource_pool"), eq(Integer.class), eq(25L), eq(10L))).thenReturn(0);
        BusinessResourceRequestDTO request = new BusinessResourceRequestDTO();
        request.setResourcePoolId(25L);
        request.setResourceKey("room-a");
        request.setLabel("Room A");
        request.setResourceType("ROOM");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.createResource(10L, request, owner));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void deleteResourceExplainsRetainedBookingHistory() {
        when(jdbcTemplate.update(contains("delete from business_resource"), eq(33L), eq(10L)))
                .thenThrow(new DataIntegrityViolationException("restricted"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteResource(10L, 33L, owner));

        assertEquals(409, error.getStatusCode().value());
    }

    private BusinessResourcePoolRequestDTO poolRequest() {
        BusinessResourcePoolRequestDTO request = new BusinessResourcePoolRequestDTO();
        request.setPoolKey(" chairs ");
        request.setLabel(" Chairs ");
        request.setResourceType(" EQUIPMENT ");
        request.setCapacity(4);
        request.setPublicLabel(" Chair ");
        request.setActive(true);
        return request;
    }
}
