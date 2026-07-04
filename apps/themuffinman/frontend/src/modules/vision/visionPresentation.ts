export const intentFamilyLabels: Record<string, string> = {
  CREATE_QUEST: "quest",
  DISCOVER_QUESTS: "quest discovery",
  VIEW_QUEST_DETAIL: "quest detail",
  CREATE_CIRCLE: "circle",
  CREATE_CIRCLE_REQUEST: "circle request",
  ACCEPT_CIRCLE_REQUEST: "circle request",
  DELETE_CIRCLE_REQUEST: "circle request",
  UPDATE_CIRCLE: "circle",
  DELETE_CIRCLE: "circle",
  CREATE_APPLICATION: "application",
  UPDATE_APPLICATION: "application",
  WITHDRAW_APPLICATION: "application",
  APPROVE_APPLICATION: "application",
  DECLINE_APPLICATION: "application",
  UPDATE_PROFILE: "profile",
  UPDATE_PROFILE_LOCATION: "profile location",
  VIEW_PROFILE: "profile",
  VIEW_SETTINGS: "settings",
  VIEW_CIRCLES: "circles",
  VIEW_CIRCLE_DETAIL: "circle detail",
  VIEW_QUEST_NEWS: "quest news",
  VIEW_APPLICATIONS: "applications",
  VIEW_APPLICATION_DETAIL: "application detail",
  OPEN_CHAT: "chat",
  VIEW_CHAT_WORKSPACE: "chat"
}

export const normalizeFamilyLabel = (value: string) => {
  const trimmed = value.trim().toLowerCase()
  const aliases: Record<string, string> = {
    quests: "quest",
    circles: "circle",
    applications: "application",
    profiles: "profile",
    chats: "chat"
  }
  return aliases[trimmed] ?? trimmed
}

export const resolveVisionFamily = (intent: string | undefined, memoryTrailFamily: string | undefined) => {
  const normalizedMemoryTrailFamily = memoryTrailFamily?.trim()
  if (normalizedMemoryTrailFamily) {
    return normalizeFamilyLabel(normalizedMemoryTrailFamily)
  }

  const normalizedIntent = intent?.trim()
  if (normalizedIntent && intentFamilyLabels[normalizedIntent]) {
    return intentFamilyLabels[normalizedIntent]
  }

  return ""
}

export const previewFieldOrderByFamily: Record<string, string[]> = {
  quest: ["quest_title", "quest_description", "reward_amount", "visibility", "schedule_mode", "scheduled_date", "scheduled_time", "location_mode", "location_label"],
  "quest discovery": ["target_quest_query", "search_query"],
  "quest detail": ["target_quest_query"],
  "quest news": ["news_count", "news_unread"],
  circle: ["circle_name", "target_circle_query"],
  "circle request": ["target_user"],
  application: ["target_quest_query", "application_message", "application_proposed_price"],
  "application detail": ["application_target_query"],
  profile: ["profile_username", "profile_description", "profile_location_mode", "profile_location_label"],
  "profile location": ["profile_location_mode", "profile_location_label"],
  settings: ["profile_location_mode", "profile_location_label"],
  circles: ["target_circle_query", "circle_name"],
  chat: ["target_user"]
}

export const previewPrimaryFieldIdsByVariant: Record<string, string[]> = {
  quest: ["quest_title", "quest_description", "reward_amount"],
  "quest discovery": ["target_quest_query", "search_query"],
  "quest detail": ["target_quest_query"],
  "quest news": ["news_count", "news_unread"],
  circle: ["circle_name", "target_circle_query"],
  "circle request": ["target_user"],
  application: ["target_quest_query", "application_message", "application_proposed_price"],
  "application detail": ["application_target_query"],
  profile: ["profile_username", "profile_description"],
  "profile location": ["profile_location_mode", "profile_location_label"],
  settings: ["profile_location_mode", "profile_location_label"],
  circles: ["target_circle_query", "circle_name"],
  chat: ["target_user"]
}

export const previewSecondaryFieldIdsByVariant: Record<string, string[]> = {
  quest: ["visibility", "schedule_mode", "scheduled_date", "scheduled_time", "location_mode", "location_label"],
  "quest discovery": [],
  "quest detail": [],
  "quest news": ["news_count", "news_unread"],
  circle: [],
  "circle request": [],
  application: [],
  "application detail": [],
  profile: ["profile_location_mode", "profile_location_label"],
  "profile location": [],
  settings: [],
  circles: [],
  chat: []
}
