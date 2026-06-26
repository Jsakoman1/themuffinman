package com.themuffinman.app.location.service;

import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DisabledLocationLookupClient implements LocationLookupClient {
    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public String providerName() {
        return "none";
    }

    @Override
    public List<LocationLookupCandidateDTO> lookup(String query) {
        return List.of();
    }

    @Override
    public LocationLookupCandidateDTO reverseLookup(BigDecimal latitude, BigDecimal longitude) {
        return null;
    }
}
