package com.themuffinman.app.identity.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentityUserSummaryAssembler {

    private final AppUserMgr appUserMgr;

    public AppUserResponseDTO buildProfileSummary(AppUser profileUser, long openQuestCount, List<QuestResponseDTO> openQuests) {
        return appUserMgr.withProfileStats(
                appUserMgr.toDto(profileUser),
                openQuestCount,
                openQuests
        );
    }

    public AppUserResponseDTO buildViewerProfileSummary(
            AppUser profileUser,
            AppUser viewer,
            long openQuestCount,
            List<QuestResponseDTO> openQuests
    ) {
        return appUserMgr.withProfileStats(
                appUserMgr.toProfileDto(profileUser, viewer),
                openQuestCount,
                openQuests
        );
    }
}
