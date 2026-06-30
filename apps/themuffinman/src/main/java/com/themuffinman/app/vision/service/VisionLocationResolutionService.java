package com.themuffinman.app.vision.service;

import com.themuffinman.app.location.dto.LocationLookupCandidateDTO;
import com.themuffinman.app.location.service.LocationLookupService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class VisionLocationResolutionService {

    private final VisionLocationParserService visionLocationParserService;
    private final LocationLookupService locationLookupService;

    public VisionLocationResolutionService(
            VisionLocationParserService visionLocationParserService,
            LocationLookupService locationLookupService
    ) {
        this.visionLocationParserService = visionLocationParserService;
        this.locationLookupService = locationLookupService;
    }

    VisionLocationResolutionService(VisionLocationParserService visionLocationParserService) {
        this.visionLocationParserService = visionLocationParserService;
        this.locationLookupService = null;
    }

    public Map<String, String> resolveCustomLocation(String rawInput, String actorKey) {
        Map<String, String> resolved = new LinkedHashMap<>(visionLocationParserService.parseCustomLocation(rawInput));
        String locationLabel = resolved.get("location_label");
        if (locationLabel == null) {
            return resolved;
        }
        if (locationLookupService == null) {
            resolved.put("location_resolution_status", "parsed_only");
            return resolved;
        }

        try {
            List<LocationLookupCandidateDTO> candidates = locationLookupService.lookupTopCandidates(locationLabel, actorKey, 3);
            if (candidates.isEmpty()) {
                resolved.put("location_resolution_status", "parsed_only");
                return resolved;
            }
            putPendingCandidates(resolved, candidates);
            resolved.put("location_resolution_status", "candidate_pending");
            putIfPresent(resolved, "location_resolution_provider", candidates.getFirst().getProvider());
            return resolved;
        } catch (ResponseStatusException exception) {
            resolved.put("location_resolution_status", "parsed_only");
            return resolved;
        }
    }

    public void confirmPendingCandidate(Map<String, String> slotData, String candidateKey) {
        if (slotData == null || candidateKey == null || candidateKey.isBlank()) {
            return;
        }
        putIfPresent(slotData, "location_label", slotData.get(candidateKey + "_label"));
        putIfPresent(slotData, "location_country_code", slotData.get(candidateKey + "_country_code"));
        putIfPresent(slotData, "location_country", slotData.get(candidateKey + "_country"));
        putIfPresent(slotData, "location_locality", slotData.get(candidateKey + "_locality"));
        putIfPresent(slotData, "location_postal_code", slotData.get(candidateKey + "_postal_code"));
        putIfPresent(slotData, "location_street", slotData.get(candidateKey + "_street"));
        putIfPresent(slotData, "location_house_number", slotData.get(candidateKey + "_house_number"));
        slotData.put("location_resolution_status", "lookup_resolved");
        clearPendingCandidate(slotData);
    }

    public void keepTypedLocation(Map<String, String> slotData) {
        if (slotData == null) {
            return;
        }
        slotData.put("location_resolution_status", "parsed_only");
        clearPendingCandidate(slotData);
    }

    private void putPendingCandidates(Map<String, String> resolved, List<LocationLookupCandidateDTO> candidates) {
        resolved.put("pending_location_candidate_count", Integer.toString(candidates.size()));
        for (int index = 0; index < candidates.size(); index++) {
            String prefix = "pending_location_candidate_" + (index + 1);
            LocationLookupCandidateDTO candidate = candidates.get(index);
            resolved.put(prefix + "_rank", Integer.toString(index + 1));
            putIfPresent(resolved, prefix + "_label", candidate.getLabel());
            putIfPresent(resolved, prefix + "_country_code", candidate.getCountryCode());
            putIfPresent(resolved, prefix + "_country", candidate.getCountry());
            putIfPresent(resolved, prefix + "_locality", candidate.getLocality());
            putIfPresent(resolved, prefix + "_postal_code", candidate.getPostalCode());
            putIfPresent(resolved, prefix + "_street", candidate.getStreet());
            putIfPresent(resolved, prefix + "_house_number", candidate.getHouseNumber());
            putIfPresent(resolved, prefix + "_match_note", buildCandidateMatchNote(resolved, candidate, index + 1));
        }
    }

    private String buildCandidateMatchNote(
            Map<String, String> typedLocation,
            LocationLookupCandidateDTO candidate,
            int rank
    ) {
        String typedStreet = normalize(typedLocation.get("location_street"));
        String typedHouseNumber = normalize(typedLocation.get("location_house_number"));
        String typedLocality = normalize(typedLocation.get("location_locality"));
        String typedPostalCode = normalize(typedLocation.get("location_postal_code"));

        boolean sameStreet = typedStreet != null && typedStreet.equals(normalize(candidate.getStreet()));
        boolean sameHouseNumber = typedHouseNumber != null && typedHouseNumber.equals(normalize(candidate.getHouseNumber()));
        boolean sameLocality = typedLocality != null && typedLocality.equals(normalize(candidate.getLocality()));
        boolean samePostalCode = typedPostalCode != null && typedPostalCode.equals(normalize(candidate.getPostalCode()));

        if (sameStreet && sameHouseNumber && sameLocality) {
            return "Top ranked match with the same street, house number, and locality as your typed place.";
        }
        if (sameStreet && sameLocality && !sameHouseNumber && typedHouseNumber != null && candidate.getHouseNumber() != null) {
            return "Candidate " + rank + " keeps the same street and locality but changes the house number to " + candidate.getHouseNumber() + ".";
        }
        if (sameStreet && sameLocality) {
            return "Candidate " + rank + " keeps the same street and locality as your typed place.";
        }
        if (sameLocality && samePostalCode) {
            return "Candidate " + rank + " matches the same postal area and locality.";
        }
        if (sameLocality) {
            return "Candidate " + rank + " matches the same locality but differs in address detail.";
        }
        return "Candidate " + rank + " is a broader backend location suggestion near the typed place.";
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private void clearPendingCandidate(Map<String, String> slotData) {
        String rawCount = slotData.get("pending_location_candidate_count");
        int count = 0;
        try {
            count = rawCount == null ? 0 : Integer.parseInt(rawCount);
        } catch (NumberFormatException ignored) {
            count = 0;
        }
        for (int index = 1; index <= Math.max(count, 3); index++) {
            String prefix = "pending_location_candidate_" + index;
            slotData.remove(prefix + "_label");
            slotData.remove(prefix + "_country_code");
            slotData.remove(prefix + "_country");
            slotData.remove(prefix + "_locality");
            slotData.remove(prefix + "_postal_code");
            slotData.remove(prefix + "_street");
            slotData.remove(prefix + "_house_number");
            slotData.remove(prefix + "_rank");
            slotData.remove(prefix + "_match_note");
        }
        slotData.remove("pending_location_candidate_count");
    }

    private void putIfPresent(Map<String, String> resolved, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        resolved.put(key, value.trim());
    }
}
