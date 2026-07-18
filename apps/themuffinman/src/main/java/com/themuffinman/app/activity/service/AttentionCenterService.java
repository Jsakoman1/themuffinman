package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("activityAttentionCenterService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttentionCenterService {
    private final ActivityReadService activityReadService;

    public List<ActivityItemDTO> getMine(AppUser user) {
        return List.copyOf(activityReadService.getMine(user));
    }

    /** A bounded quick-switcher projection; it shares the same viewer authority as attention. */
    public List<ActivityItemDTO> getRecent(AppUser user) { return List.copyOf(activityReadService.getRecent(user)); }
}
