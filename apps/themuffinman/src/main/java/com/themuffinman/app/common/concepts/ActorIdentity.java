package com.themuffinman.app.common.concepts;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;

public record ActorIdentity(Long userId, AppUserRole role) {

    public static ActorIdentity anonymous() {
        return new ActorIdentity(null, null);
    }

    public static ActorIdentity from(AppUser user) {
        if (user == null) {
            return anonymous();
        }
        return new ActorIdentity(user.getId(), user.getRole());
    }

    public boolean authenticated() {
        return userId != null;
    }

    public boolean admin() {
        return role == AppUserRole.ADMIN;
    }

    public boolean sameUser(Long otherUserId) {
        return userId != null && userId.equals(otherUserId);
    }
}
