package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionIntent;

/** Builds a viewer-authorized candidate context for one semantic family. */
public interface VisionCandidateContextProvider {
    boolean supports(VisionIntent intent);

    VisionCandidateContext build(AppUser currentUser, VisionIntent intent, String requestId, String rawPrompt);
}
