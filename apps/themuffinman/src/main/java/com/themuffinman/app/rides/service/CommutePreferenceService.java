package com.themuffinman.app.rides.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.CommutePreferenceRequestDTO;
import com.themuffinman.app.rides.dto.CommutePreferenceResponseDTO;
import com.themuffinman.app.rides.model.CommutePreference;
import com.themuffinman.app.rides.repository.CommutePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommutePreferenceService {
    private final CommutePreferenceRepository repository;

    public CommutePreferenceResponseDTO getMine(AppUser user) {
        return repository.findByUserId(user.getId()).map(this::toDto).orElseGet(() -> CommutePreferenceResponseDTO.builder().enabled(false).consentGranted(false).weekdays(List.of()).build());
    }

    @Transactional
    public CommutePreferenceResponseDTO updateMine(CommutePreferenceRequestDTO dto, AppUser user) {
        if (dto == null) throw ServiceErrors.badRequest("Commute preference request is required");
        List<Integer> weekdays = normalizeWeekdays(dto.getWeekdays());
        String home = trim(dto.getHomeArea());
        String work = trim(dto.getWorkArea());
        if (dto.isEnabled() && !dto.isConsentGranted()) throw ServiceErrors.badRequest("Commute matching requires explicit consent");
        if (dto.isEnabled() && (home == null || work == null || dto.getDepartureTime() == null || weekdays.isEmpty())) {
            throw ServiceErrors.badRequest("Enabled commute matching requires approximate areas, weekdays, and departure time");
        }
        CommutePreference preference = repository.findByUserId(user.getId()).orElseGet(() -> {
            CommutePreference created = new CommutePreference();
            created.setUser(user);
            return created;
        });
        preference.setEnabled(dto.isEnabled());
        preference.setConsentGranted(dto.isConsentGranted());
        preference.setHomeArea(home);
        preference.setWorkArea(work);
        preference.setWeekdays(weekdays.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null));
        preference.setDepartureTime(dto.getDepartureTime());
        preference.setReturnTime(dto.getReturnTime());
        return toDto(repository.save(preference));
    }

    private List<Integer> normalizeWeekdays(List<Integer> values) {
        if (values == null) return List.of();
        if (values.stream().anyMatch(value -> value == null || value < 1 || value > 7)) throw ServiceErrors.badRequest("Weekdays must use ISO values 1 through 7");
        return values.stream().distinct().sorted().toList();
    }

    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private CommutePreferenceResponseDTO toDto(CommutePreference preference) {
        List<Integer> weekdays = preference.getWeekdays() == null || preference.getWeekdays().isBlank() ? List.of() : Arrays.stream(preference.getWeekdays().split(",")).map(Integer::parseInt).toList();
        return CommutePreferenceResponseDTO.builder().id(preference.getId()).enabled(preference.isEnabled()).consentGranted(preference.isConsentGranted()).homeArea(preference.getHomeArea()).workArea(preference.getWorkArea()).weekdays(weekdays).departureTime(preference.getDepartureTime()).returnTime(preference.getReturnTime()).updatedAt(preference.getUpdatedAt()).build();
    }
}
