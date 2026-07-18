package com.themuffinman.app.things.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingPreviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThingPreviewReadService {

    private final ThingSharingService thingSharingService;

    public ThingPreviewResponseDTO getPreview(long listingId, AppUser viewer) {
        return ThingPreviewResponseDTO.from(thingSharingService.getListingDetail(listingId, viewer));
    }
}
