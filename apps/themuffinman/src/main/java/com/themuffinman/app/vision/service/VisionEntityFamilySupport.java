package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionIntent;

final class VisionEntityFamilySupport {

    private VisionEntityFamilySupport() {
    }

    static String conversationFamilyLabel(VisionIntent intent) {
        if (intent == null) {
            return null;
        }
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, VIEW_USER_PROFILE -> "profile";
            case VIEW_NOTIFICATIONS -> "notifications";
            case VIEW_QUEST_NEWS -> "quest news";
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> "business";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case DISCOVER_QUESTS, CREATE_QUEST, VIEW_QUEST_DETAIL -> "quests";
            case SEARCH -> "search";
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> "chat";
            default -> null;
        };
    }

    static String learningFamilyLabel(VisionIntent intent) {
        if (intent == null) {
            return "other";
        }
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> "profile";
            case VIEW_NOTIFICATIONS -> "notifications";
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> "business";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case DISCOVER_QUESTS, CREATE_QUEST, VIEW_QUEST_DETAIL -> "quests";
            case VIEW_QUEST_NEWS -> "quest news";
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> "chat";
            case SEARCH -> "search";
            default -> "other";
        };
    }
}
