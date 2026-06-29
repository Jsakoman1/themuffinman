package com.themuffinman.app.common.concepts;

import com.themuffinman.app.identity.model.AppUser;

public final class ModuleOwnership {

    private ModuleOwnership() {
    }

    public static boolean isOwner(Long ownerUserId, AppUser actor) {
        return isOwner(ownerUserId, ActorIdentity.from(actor));
    }

    public static boolean isOwner(Long ownerUserId, ActorIdentity actor) {
        return ownerUserId != null && actor != null && actor.sameUser(ownerUserId);
    }

    public static boolean canManage(Long ownerUserId, AppUser actor) {
        return canManage(ownerUserId, ActorIdentity.from(actor));
    }

    public static boolean canManage(Long ownerUserId, ActorIdentity actor) {
        return actor != null && (actor.admin() || isOwner(ownerUserId, actor));
    }
}
