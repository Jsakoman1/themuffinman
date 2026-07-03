package com.themuffinman.app.semantic;

import com.themuffinman.app.identity.model.AppUser;

public interface VisionEntityResolver<T> {
    SemanticEntityFamily family();

    SemanticEntityResolution<T> resolve(AppUser currentUser, String targetEntityQuery);
}
