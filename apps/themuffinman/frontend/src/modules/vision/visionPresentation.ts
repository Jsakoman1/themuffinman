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

export const visionSlotLabels: Record<string, string> = {
  circle_name: "Name",
  target_circle_query: "Circle",
  target_user: "Person",
  quest_title: "Name",
  quest_description: "Details",
  target_quest_query: "Quest",
  application_message: "Message",
  application_proposed_price: "Price",
  profile_username: "Username",
  profile_description: "Bio",
  profile_location_mode: "Location mode",
  profile_location_label: "Place",
  reward_amount: "Reward",
  visibility: "Visibility",
  schedule_mode: "Schedule",
  scheduled_date: "Date",
  scheduled_time: "Time",
  location_mode: "Location",
  location_label: "Place",
  location_candidate_confirmation: "Confirm place"
}

export const visionSlotPlaceholders: Record<string, string> = {
  circle_name: "Name the circle",
  target_circle_query: "Say the circle name or id",
  target_user: "Say the exact username, email, or name fragment",
  quest_title: "Name the quest",
  quest_description: "Describe the task",
  target_quest_query: "Say the quest title or id",
  application_message: "Write the message",
  application_proposed_price: "Example: 20 or 20.50",
  profile_username: "Choose the username",
  profile_description: "Write a short bio",
  profile_location_mode: "Choose off, approximate, or exact",
  profile_location_label: "Example: Zurich, Switzerland",
  reward_amount: "Example: 20 euros or free",
  visibility: "Example: public or circles",
  schedule_mode: "Example: fixed time or by agreement",
  scheduled_date: "Example: 2026-07-03 or next Tuesday",
  scheduled_time: "Example: 14:30 or 2 pm",
  location_mode: "Example: use profile, hide location, or custom place",
  location_label: "Example: Ban Jelacic Square, Zagreb",
  location_candidate_confirmation: "Choose resolved place or keep typed place"
}

export const formatVisionFieldRequestLabel = (slotId: string | null | undefined, fallbackTitle: string | null | undefined) => {
  if (!slotId) {
    return ""
  }
  return visionSlotLabels[slotId] ?? fallbackTitle?.trim() ?? "Next field"
}

export const formatVisionFieldRequestPlaceholder = (slotId: string | null | undefined, fallbackPlaceholder: string | null | undefined) => {
  if (slotId) {
    return fallbackPlaceholder?.trim() || visionSlotPlaceholders[slotId] || "Type the next detail"
  }
  return fallbackPlaceholder?.trim() || "Type a command."
}

export const formatVisionFlowLine = (slotLabel: string, slotValue: string) => {
  const trimmedSlotLabel = slotLabel.trim()
  const trimmedSlotValue = slotValue.trim()
  if (!trimmedSlotLabel) {
    return ""
  }
  return trimmedSlotValue ? `next ${trimmedSlotLabel}: ${trimmedSlotValue}` : `next ${trimmedSlotLabel}`
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
