package com.themuffinman.app.location.service;

import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;

import java.math.BigDecimal;
import java.util.List;

public interface LocationLookupClient {
    boolean isConfigured();
    String providerName();
    List<LocationLookupCandidateDTO> lookup(String query);
    LocationLookupCandidateDTO reverseLookup(BigDecimal latitude, BigDecimal longitude);
}
