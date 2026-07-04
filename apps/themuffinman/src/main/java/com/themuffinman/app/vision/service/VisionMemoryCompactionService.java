package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.config.VisionProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisionMemoryCompactionService {

    private final AppUserRepository appUserRepository;
    private final VisionLearningService visionLearningService;
    private final VisionProperties visionProperties;

    public VisionMemoryCompactionService(
            AppUserRepository appUserRepository,
            VisionLearningService visionLearningService,
            VisionProperties visionProperties
    ) {
        this.appUserRepository = appUserRepository;
        this.visionLearningService = visionLearningService;
        this.visionProperties = visionProperties;
    }

    @Scheduled(cron = "${app.vision.memory.compaction-cron:0 20 4 * * *}")
    public void compactMemory() {
        if (!visionProperties.isEnabled()) {
            return;
        }

        List<AppUser> users = appUserRepository.findAll();
        for (AppUser user : users) {
            visionLearningService.compactMemoryForUser(user);
        }
    }
}
