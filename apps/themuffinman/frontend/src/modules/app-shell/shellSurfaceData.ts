import {computed, ref, toValue, watch, type MaybeRefOrGetter} from "vue"
import type {RouteLocationRaw, RouteLocationNormalizedLoaded} from "vue-router"
import type {
  BusinessBookingResponseDTO,
  BusinessOwnerCalendarItemDTO,
  BusinessOwnerDashboardDTO,
  BusinessProfileResponseDTO,
  ChatConversationSummaryDTO,
  ChatMessageDTO,
  CircleContactDTO,
  CircleRequestResponseDTO,
  DashboardPlannerItemDTO,
  QuestApplicationResponseDTO,
  QuestResponseDTO,
  UserProfileViewDTO
} from "../../contracts/index.ts"
import {currentUser} from "../identity/auth.ts"
import {getAppSurfaceConfig, type AppSurfaceId} from "./shellDefinitions.ts"
import {resolveSurfaceDetailRoute} from "./shellRouteRegistry.ts"
import {userShellApi} from "./api/userShellApi.ts"
import {buildSurfaceVisionRoute} from "./visionHandoff.ts"
import {formatDate, formatDateTime, formatNumber} from "../../services/formatters.ts"

export type ShellSurfaceMetric = {
  label: string
  value: string
  detail: string
  to?: RouteLocationRaw
  tone?: "default" | "emphasis"
}

export type ShellSurfaceRow = {
  id: string
  title: string
  description: string
  meta?: string
  thumbnailUrl?: string
  badge?: string
  to?: RouteLocationRaw
  visionTo?: RouteLocationRaw
  startAt?: string | null
  endAt?: string | null
  eventType?: "work" | "business"
}

export type ShellSurfaceSection = {
  title: string
  description: string
  emptyState: string
  rows: ShellSurfaceRow[]
}

export type ShellSurfaceViewModel = {
  metrics: ShellSurfaceMetric[]
  sections: ShellSurfaceSection[]
  note?: string
}

const safeRequest = async <T>(request: () => Promise<T>) => {
  try {
    return await request()
  } catch {
    return null
  }
}

const formatCount = (value: number) => formatNumber(value, "en-US")

const homeMetricRoute = (scope: string): RouteLocationRaw => ({path: "/work/find", query: {scope}})

const describeQuestTerm = (quest: QuestResponseDTO) => {
  if (quest.scheduledAt && quest.endsAt) {
    return `${formatDateTime(quest.scheduledAt)} to ${formatDateTime(quest.endsAt)}`
  }
  if (quest.scheduledAt) {
    return formatDateTime(quest.scheduledAt)
  }
  return quest.presentation.timeTypeLabel
}

const questRoute = (questId: number): RouteLocationRaw => resolveSurfaceDetailRoute("work-quests", questId) ?? {path: `/work/quests/${questId}`}
const applicationRoute = (applicationId: number): RouteLocationRaw => resolveSurfaceDetailRoute("work-applications", applicationId) ?? {path: `/work/applications/${applicationId}`}
const chatRoute = (conversationId: number): RouteLocationRaw => resolveSurfaceDetailRoute("chat-conversation", conversationId) ?? {path: `/chat/${conversationId}`}

const createQuestRow = (quest: QuestResponseDTO): ShellSurfaceRow => ({
  id: `quest-${quest.id}`,
  title: quest.title,
  description: quest.presentation.statusLabel,
  meta: describeQuestTerm(quest),
  badge: quest.status,
  to: questRoute(quest.id),
  visionTo: buildSurfaceVisionRoute("work", "/work", "Work")
})

const createApplicationRow = (application: QuestApplicationResponseDTO): ShellSurfaceRow => ({
  id: `application-${application.id}`,
  title: application.questTitle,
  description: application.presentation.statusLabel,
  meta: `${application.questCreatorUsername} · ${formatDate(application.createdAt)}`,
  badge: application.status,
  to: applicationRoute(application.id),
  visionTo: buildSurfaceVisionRoute("work-applications", "/work/applications", "My applications")
})

const createChatRow = (conversation: ChatConversationSummaryDTO): ShellSurfaceRow => ({
  id: `conversation-${conversation.conversationId}`,
  title: conversation.title ?? conversation.otherUsername ?? `Conversation #${conversation.conversationId}`,
  description: conversation.lastMessagePreview ?? "No messages yet.",
  meta: conversation.lastMessageAt ? formatDateTime(conversation.lastMessageAt) : `${conversation.participantCount} participants`,
  badge: conversation.unreadCount > 0 ? `${conversation.unreadCount} unread` : undefined,
  to: chatRoute(conversation.conversationId),
  visionTo: buildSurfaceVisionRoute("chat", "/chat", "Chat")
})

const createMessageRow = (message: ChatMessageDTO): ShellSurfaceRow => ({
  id: `message-${message.id}`,
  title: message.senderUsername,
  description: message.messageBody ?? message.attachmentName ?? "Attachment",
  meta: formatDateTime(message.createdAt),
  badge: message.ownMessage ? "You" : undefined
})

const createBookingRow = (booking: BusinessBookingResponseDTO): ShellSurfaceRow => ({
  id: `booking-${booking.id}`,
  title: booking.businessOfferingTitle,
  description: `${booking.customerUsername} · ${booking.statusLabel}`,
  meta: formatDateTime(booking.startsAt),
  badge: booking.status,
  to: {path: "/business/bookings"}
})

const createCalendarRow = (item: BusinessOwnerCalendarItemDTO): ShellSurfaceRow => ({
  id: `calendar-booking-${item.bookingId}`,
  title: item.businessOfferingTitle,
  description: `${item.customerUsername} · ${item.statusLabel}`,
  meta: `${formatDateTime(item.startsAt)} to ${formatDateTime(item.endsAt)}`,
  badge: item.status,
  to: {path: "/business/calendar"},
  startAt: item.startsAt,
  endAt: item.endsAt,
  eventType: "business"
})

const createPlannerRow = (item: DashboardPlannerItemDTO): ShellSurfaceRow => ({
  id: item.id,
  title: item.title,
  description: item.kindLabel,
  meta: item.scheduledAt ? describePlannerTerm(item) : "Flexible timing",
  badge: item.kind,
  to: item.questId ? questRoute(item.questId) : undefined,
  visionTo: buildSurfaceVisionRoute("calendar", "/calendar", "Calendar"),
  startAt: item.scheduledAt,
  endAt: item.endsAt,
  eventType: "work"
})

const describePlannerTerm = (item: DashboardPlannerItemDTO) => {
  if (item.scheduledAt && item.endsAt) {
    return `${formatDateTime(item.scheduledAt)} to ${formatDateTime(item.endsAt)}`
  }
  return formatDateTime(item.scheduledAt)
}

const createCircleRequestRow = (request: CircleRequestResponseDTO): ShellSurfaceRow => ({
  id: `circle-request-${request.id}`,
  title: request.counterpartUsername,
  description: request.requestSummaryLabel,
  meta: formatDateTime(request.createdAt),
  badge: request.resolutionLabel ?? undefined,
  to: {path: "/circles"}
})

const createCircleContactRow = (contact: CircleContactDTO): ShellSurfaceRow => ({
  id: `circle-contact-${contact.userId}`,
  title: contact.username,
  description: contact.profileDescription || "No profile description yet.",
  meta: contact.circleSummaryLabel,
  badge: contact.circleNames[0] ?? undefined,
  to: {path: `/people/${contact.userId}`}
})

const createProfileActionRow = (profileView: UserProfileViewDTO): ShellSurfaceRow[] => {
  const rows: ShellSurfaceRow[] = []

  rows.push({
    id: "profile-primary-action",
    title: profileView.primaryAction.label,
    description: profileView.primaryAction.enabled
      ? "Available from the current profile context."
      : "Not available in the current context.",
    badge: profileView.primaryAction.enabled ? "Ready" : "Unavailable",
    visionTo: buildSurfaceVisionRoute("profile", "/profile", "Profile")
  })

  if (profileView.showBlockAction) {
    rows.push({
      id: "profile-block-action",
      title: profileView.blockActionLabel,
      description: profileView.blockActionEnabled
        ? "Available from the current profile context."
        : "Blocked in the current context.",
      badge: profileView.blockActionEnabled ? "Ready" : "Unavailable"
    })
  }

  return rows
}

const loadHomeData = async (): Promise<ShellSurfaceViewModel> => {
  const summary = await userShellApi.getDashboardSummary()

  return {
    metrics: [
      {
        label: "Open work",
        value: formatCount(summary.openQuestCount),
        detail: "Available work visible to you.",
        to: homeMetricRoute("open-visible"),
        tone: "emphasis"
      },
      {
        label: "My work",
        value: formatCount(summary.visibleMyQuestsCount),
        detail: "Work you created and can manage.",
        to: {path: "/work/quests", query: {scope: "owned-visible"}}
      },
      {
        label: "Pending applications",
        value: formatCount(summary.pendingWorkApplicationsCount),
        detail: "Applications waiting on a next step.",
        to: {path: "/work/applications", query: {scope: "pending"}}
      },
      {
        label: "Unread news",
        value: formatCount(summary.unreadNewsCount),
        detail: "Signals that may require follow-up.",
        to: {path: "/notifications"}
      }
    ],
    sections: [
      {
        title: "Orientation",
        description: "Home stays light and only reflects backend-prepared work summary.",
        emptyState: "No summary signals are available yet.",
        rows: [
          {
            id: "home-active-work",
            title: `${formatCount(summary.activeWorkCount)} active work items`,
            description: "A combined count of work currently in motion.",
            badge: "Work",
            to: {path: "/work"}
          },
          {
            id: "home-assigned-work",
            title: `${formatCount(summary.assignedQuestCount)} assigned quests`,
            description: "Work already assigned and still active.",
            badge: "Assigned",
            to: {path: "/work/quests"}
          },
          {
            id: "home-completed-work",
            title: `${formatCount(summary.completedMyQuestsCount)} completed quests`,
            description: "Closed work that remains visible in your history.",
            badge: "History",
            to: {path: "/work/quests"}
          }
        ]
      }
    ],
    note: "Home is a compact orientation layer; Notifications cover unread delivery while Activity is the deduplicated resume timeline."
  }
}

const loadWorkData = async (surfaceId: AppSurfaceId): Promise<ShellSurfaceViewModel> => {
  const [dashboard, applications] = await Promise.all([
    userShellApi.getDashboard(),
    surfaceId === "work-applications" ? safeRequest(() => userShellApi.getMyApplications()) : Promise.resolve(null)
  ])

  const discoverRows = dashboard.availableQuests.map(createQuestRow)
  const myQuestRows = (dashboard.sections.recentMyQuests.length > 0
    ? dashboard.sections.recentMyQuests
    : dashboard.myQuests).map(createQuestRow)
  const applicationRows = (applications?.items ?? (dashboard.sections.recentMyApplications.length > 0
    ? dashboard.sections.recentMyApplications
    : dashboard.myApplications)).map(createApplicationRow)

  if (surfaceId === "work-quests") {
    return {
      metrics: [
        {
          label: "Active quests",
          value: formatCount(dashboard.summary.activeMyQuestsCount),
          detail: "Your owned quests still in motion.",
          tone: "emphasis"
        },
        {
          label: "Visible quests",
          value: formatCount(dashboard.summary.visibleMyQuestsCount),
          detail: "Quests currently visible in your workspace."
        }
      ],
      sections: [
        {
          title: "My work",
          description: "Stable list entry with Vision-native detail continuity.",
          emptyState: "No owned quests are available right now.",
          rows: myQuestRows
        }
      ]
    }
  }

  if (surfaceId === "work-applications") {
    return {
      metrics: [
        {
          label: "Pending applications",
          value: formatCount(dashboard.summary.pendingWorkApplicationsCount),
          detail: "Applications waiting for progress.",
          tone: "emphasis"
        },
        {
          label: "Active applications",
          value: formatCount(dashboard.summary.activeWorkApplicationsCount),
          detail: "Applications with ongoing thread or review activity."
        }
      ],
      sections: [
        {
          title: "My applications",
          description: "Deterministic applications browse with Vision-native detail.",
          emptyState: "No applications are visible right now.",
          rows: applicationRows
        }
      ]
    }
  }

  return {
    metrics: [
      {
        label: "Open quests",
        value: formatCount(dashboard.summary.openQuestCount),
        detail: "Available work visible right now.",
        tone: "emphasis"
      },
      {
        label: "My active quests",
        value: formatCount(dashboard.summary.activeMyQuestsCount),
        detail: "Owned work that needs active management."
      },
      {
        label: "Pending applications",
        value: formatCount(dashboard.summary.pendingWorkApplicationsCount),
        detail: "Application follow-up still waiting."
      }
    ],
    sections: [
      {
        title: "Discover",
        description: "Available work from the backend dashboard read model.",
        emptyState: "No open work is available right now.",
        rows: discoverRows
      },
      {
        title: "My work",
        description: "Owned work remains easy to scan from one stable lane.",
        emptyState: "No owned quests are visible right now.",
        rows: myQuestRows
      },
      {
        title: "My applications",
        description: "Applications you submitted for work, with their current status and next actions.",
        emptyState: "No applications are visible right now.",
        rows: applicationRows
      }
    ]
  }
}

const loadChatData = async (route: RouteLocationNormalizedLoaded): Promise<ShellSurfaceViewModel> => {
  const workspace = await userShellApi.getChatWorkspace()
  const conversationId = Number(route.params.conversationId)
  const conversationSync = Number.isFinite(conversationId)
    ? await safeRequest(() => userShellApi.getChatConversationSync(conversationId))
    : null

  const sections: ShellSurfaceSection[] = [
    {
      title: "Chat",
      description: "Chat owns deterministic thread browsing.",
      emptyState: "No conversations are available right now.",
      rows: workspace.conversations.map(createChatRow)
    },
    {
      title: "Contacts",
      description: "People you can coordinate with directly.",
      emptyState: "No contacts are available right now.",
      rows: workspace.contacts.map((contact) => ({
        id: `chat-contact-${contact.userId}`,
        title: contact.username,
        description: contact.profileDescription || "No profile description yet.",
        meta: contact.circleNames.join(", ") || contact.resolutionLabel,
        badge: contact.circleNames[0] ?? undefined
      }))
    }
  ]

  if (conversationSync) {
    sections.unshift({
      title: "Current conversation",
      description: "Stable thread route remains chat-owned.",
      emptyState: "No messages are available yet.",
      rows: [...conversationSync.messages].reverse().map(createMessageRow)
    })
  }

  return {
    metrics: [
      {
        label: "Unread",
        value: formatCount(workspace.unreadConversationCount),
        detail: "Unread conversation count.",
        tone: "emphasis"
      },
      {
        label: "Conversations",
        value: formatCount(workspace.conversations.length),
        detail: "Visible threads in your current workspace."
      },
      {
        label: "Contacts online",
        value: formatCount(workspace.onlineContactCount),
        detail: "Available people currently online."
      }
    ],
    sections
  }
}

const loadBusinessData = async (surfaceId: AppSurfaceId): Promise<ShellSurfaceViewModel> => {
  // Owner dashboard/calendar require an active business profile. Resolve the
  // profile first so an unconfigured account does not generate expected 400s.
  const [profile, bookings] = await Promise.all([
    safeRequest(() => userShellApi.getBusinessProfile()),
    safeRequest(() => userShellApi.getBusinessOwnerBookings())
  ])
  const [dashboard, calendar] = profile
    ? await Promise.all([
      safeRequest(() => userShellApi.getBusinessDashboard()),
      safeRequest(() => userShellApi.getBusinessOwnerCalendar())
    ])
    : [null, null]

  if (!dashboard && !profile) {
    return {
      metrics: [],
      sections: [
        {
        title: "Business setup",
        description: "No business owner read model is available for this account yet.",
        emptyState: "Open Vision to set up or continue your business profile.",
        rows: [{
          id: "business-setup-profile",
          title: "Set up your business profile",
          description: "Add the public identity customers will see.",
          badge: "Next step",
          to: {path: "/business/profile"}
        }]
        }
      ],
      note: "Business entry stays real, but this account does not currently expose the owner read surfaces."
    }
  }

  const metrics: ShellSurfaceMetric[] = dashboard
    ? [
      {
        label: "Active offerings",
        value: formatCount(dashboard.activeOfferingCount),
        detail: "Offerings currently visible to customers.",
        to: {path: "/business/offerings"},
        tone: "emphasis"
      },
      {
        label: "Pending confirmations",
        value: formatCount(dashboard.pendingConfirmationCount),
        detail: "Bookings waiting for owner action.",
        to: {path: "/business/bookings"}
      },
      {
        label: "Today",
        value: formatCount(dashboard.todayCount),
        detail: "Bookings on today's schedule.",
        to: {path: "/business/calendar"}
      },
      {
        label: "Upcoming",
        value: formatCount(dashboard.upcomingCount),
        detail: "Near-term business workload.",
        to: {path: "/business/calendar"}
      }
    ]
    : []

  const profileRows = profile ? createBusinessProfileRows(profile, dashboard) : []
  const bookingRows = bookings?.items.map(createBookingRow) ?? []
  const calendarRows = calendar?.days.flatMap((day) => day.items).map(createCalendarRow) ?? []

  if (surfaceId === "business-profile") {
    return {
      metrics,
      sections: [
        {
          title: "Profile",
          description: "Business identity remains business-owned.",
          emptyState: "No business profile is available yet.",
          rows: profileRows
        }
      ]
    }
  }

  if (surfaceId === "business-bookings") {
    return {
      metrics,
      sections: [
        {
          title: "Bookings",
          description: "Operational bookings remain in the business route family.",
          emptyState: "No owner bookings are available right now.",
          rows: bookingRows
        }
      ]
    }
  }

  if (surfaceId === "business-calendar") {
    return {
      metrics,
      sections: [
        {
          title: "Owner calendar",
          description: "Time remains visible without stealing detail ownership.",
          emptyState: "No owner calendar entries are available right now.",
          rows: calendarRows
        }
      ]
    }
  }

  return {
    metrics,
    sections: [
      {
        title: "Business profile",
        description: "Owner identity and public business shape.",
        emptyState: "No business profile is available yet.",
        rows: profileRows.length > 0 ? profileRows : [{
          id: "business-setup-profile",
          title: "Set up your business profile",
          description: "Add the public identity customers will see.",
          badge: "Next step",
          to: {path: "/business/profile"}
        }]
      },
      {
        title: "Upcoming bookings",
        description: "Operational owner bookings from the backend read model.",
        emptyState: "No owner bookings are available right now.",
        rows: bookingRows
      },
      {
        title: "Calendar pulse",
        description: "Near-term time slots from the owner calendar read model.",
        emptyState: "No owner calendar entries are available right now.",
        rows: calendarRows
      }
    ]
  }
}

const createBusinessProfileRows = (profile: BusinessProfileResponseDTO, dashboard: BusinessOwnerDashboardDTO | null): ShellSurfaceRow[] => [
  {
    id: `business-profile-${profile.id}`,
    title: profile.businessName,
    description: profile.headline || "No headline yet.",
    meta: profile.slug,
    badge: profile.active ? "Active" : "Draft",
    to: {path: "/business/profile"}
  },
  {
    id: `business-booking-enabled-${profile.id}`,
    title: profile.bookingEnabled ? "Booking is enabled" : "Booking is disabled",
    description: profile.contactEmail || "No contact email",
    meta: dashboard ? `${formatCount(dashboard.activeOfferingCount)} active offerings` : profile.timezone,
    badge: profile.timezone,
    to: {path: "/business/offerings"}
  }
]

const loadCalendarData = async (): Promise<ShellSurfaceViewModel> => {
  const [dashboard, profile] = await Promise.all([
    safeRequest(() => userShellApi.getDashboard()),
    safeRequest(() => userShellApi.getBusinessProfile())
  ])
  const businessCalendar = profile
    ? await safeRequest(() => userShellApi.getBusinessOwnerCalendar())
    : null

  if (!dashboard) {
    throw new Error("Could not load calendar data.")
  }

  const plannerRows = dashboard?.sections.planner.scheduledItems.map(createPlannerRow) ?? []
  const flexibleRows = dashboard?.sections.planner.flexibleItems.map(createPlannerRow) ?? []
  const businessRows = businessCalendar?.days.flatMap((day) => day.items).map(createCalendarRow) ?? []

  const metrics: ShellSurfaceMetric[] = [
    {
      label: "Scheduled work",
      value: formatCount(dashboard?.sections.planner.scheduledItems.length ?? 0),
      detail: "Work items with explicit time placement.",
      tone: "emphasis"
    },
    {
      label: "Flexible work",
      value: formatCount(dashboard?.sections.planner.flexibleItems.length ?? 0),
      detail: "Work items that still need time assignment."
    },
    {
      label: "Business bookings",
      value: formatCount(businessCalendar?.totalBookings ?? 0),
      detail: "Owner bookings in the current calendar window."
    }
  ]

  return {
    metrics,
    sections: [
      {
        title: "Work schedule",
        description: "Time-indexed work from the backend planner section.",
        emptyState: "No scheduled work is available right now.",
        rows: plannerRows
      },
      {
        title: "Business calendar",
        description: "Owner business time from the calendar read model.",
        emptyState: "No business calendar entries are available right now.",
        rows: businessRows
      },
      {
        title: "Flexible items",
        description: "Items that still need time planning.",
        emptyState: "No flexible items are available right now.",
        rows: flexibleRows
      }
    ],
    note: businessCalendar
      ? `Calendar stays a coordination index in ${businessCalendar.timezone} and routes users back to the owning work or business surface for detail.`
      : "Business calendar data is temporarily unavailable; available work schedule data remains visible."
  }
}

const loadCirclesData = async (): Promise<ShellSurfaceViewModel> => {
  const [overview, incomingRequests, connections] = await Promise.all([
    userShellApi.getCirclesOverview(),
    userShellApi.getIncomingCircleRequests(),
    userShellApi.getCircleConnections()
  ])

  const metrics: ShellSurfaceMetric[] = [
    {
      label: "Connections",
      value: formatCount(overview.connectionCount),
      detail: "People currently inside your trusted graph.",
      to: {path: "/people"},
      tone: "emphasis"
    },
    {
      label: "Incoming",
      value: formatCount(overview.incomingRequestCount),
      detail: "Requests waiting for your attention.",
      to: {path: "/circles"}
    },
    {
      label: "Outgoing",
      value: formatCount(overview.outgoingRequestCount),
      detail: "Requests still awaiting the other person.",
      to: {path: "/circles"}
    }
  ]

  return {
    metrics: [
      ...metrics
    ],
    sections: [
      {
        title: "Incoming requests",
        description: "Pending relationship requests from the circles read model.",
        emptyState: "No incoming requests are waiting right now.",
        rows: incomingRequests.items.map(createCircleRequestRow)
      },
      {
        title: "Connections",
        description: "Trusted people you can act with from module space.",
        emptyState: "No connections are available right now.",
        rows: connections.items.map(createCircleContactRow)
      }
    ]
  }
}

const loadProfileData = async (surfaceId: AppSurfaceId): Promise<ShellSurfaceViewModel> => {
  const appUser = await userShellApi.getCurrentAppUser()
  const profileView = await safeRequest(() => userShellApi.getCurrentProfileView(appUser.id))

  const profileRows: ShellSurfaceRow[] = [
    {
      id: `profile-${appUser.id}`,
      title: appUser.username,
      description: appUser.profileDescription || "No profile description yet.",
      meta: appUser.email,
      badge: appUser.role,
      to: {path: "/profile"}
    },
    {
      id: `profile-open-quests-${appUser.id}`,
      title: `${formatCount(appUser.openQuestCount)} open quests`,
      description: "Current profile-facing quest footprint.",
      meta: formatDate(appUser.createdAt),
      badge: appUser.locationSettings.locality || appUser.locationSettings.label || "Profile",
      to: {path: "/work/quests"}
    }
  ]

  const actionRows = profileView ? createProfileActionRow(profileView) : []
  const reviewRows = profileView?.recentReviews.map((review) => ({
    id: `review-${review.id}`,
    title: `${review.reviewerUsername} · ${review.stars}/5`,
    description: review.comment || "No written comment.",
    meta: formatDate(review.createdAt)
  })) ?? []

  if (surfaceId === "profile-settings") {
    return {
      metrics: [
        {
          label: "Profile visibility",
          value: profileView?.relation.relationLabel ?? "Self",
          detail: "Current profile visibility context.",
          to: {path: "/profile/settings"},
          tone: "emphasis"
        }
      ],
      sections: [
        {
          title: "Settings entry",
          description: "Settings stay nested under Profile in phase one.",
          emptyState: "Profile settings guidance is not available yet.",
          rows: actionRows
        }
      ],
      note: "Detailed settings editing remains guided through Vision-native continuity in this phase."
    }
  }

  const metrics: ShellSurfaceMetric[] = [
    {
      label: "Employer rating",
      value: profileView ? `${profileView.employerRating.averageStars.toFixed(1)}/5` : "N/A",
      detail: profileView ? `${formatCount(profileView.employerRating.reviewCount)} reviews` : "No rating summary available.",
      to: {path: "/profile"},
      tone: "emphasis"
    },
    {
      label: "Worker rating",
      value: profileView ? `${profileView.workerRating.averageStars.toFixed(1)}/5` : "N/A",
      detail: profileView ? `${formatCount(profileView.workerRating.reviewCount)} reviews` : "No rating summary available.",
      to: {path: "/profile"}
    }
  ]

  return {
    metrics,
    sections: [
      {
        title: "Profile",
        description: "Calm self-facing information from existing backend reads.",
        emptyState: "No profile information is available right now.",
        rows: profileRows
      },
      {
        title: "Available actions",
        description: "Profile-scoped actions remain explicit and route-safe.",
        emptyState: "No direct profile actions are available right now.",
        rows: actionRows
      },
      {
        title: "Recent reviews",
        description: "Recent social proof from the current profile view read model.",
        emptyState: "No reviews are visible right now.",
        rows: reviewRows
      }
    ]
  }
}

const resolveSurfaceData = async (surfaceId: AppSurfaceId, route: RouteLocationNormalizedLoaded) => {
  switch (surfaceId) {
    case "home":
      return loadHomeData()
    case "work":
    case "work-quests":
    case "work-applications":
      return loadWorkData(surfaceId)
    case "chat":
    case "chat-conversation":
      return loadChatData(route)
    case "calendar":
      return loadCalendarData()
    case "business":
    case "business-profile":
    case "business-bookings":
    case "business-calendar":
      return loadBusinessData(surfaceId)
    case "circles":
      return loadCirclesData()
    case "profile":
    case "profile-settings":
      return loadProfileData(surfaceId)
    case "notifications":
    case "activity":
      return {metrics: [], sections: []}
    default:
      return {metrics: [], sections: []}
  }
}

export const useShellSurfaceData = (
  surfaceIdSource: MaybeRefOrGetter<AppSurfaceId>,
  routeSource: MaybeRefOrGetter<RouteLocationNormalizedLoaded>
) => {
  const model = ref<ShellSurfaceViewModel>({
    metrics: [],
    sections: []
  })
  const isLoading = ref(true)
  const error = ref("")

  const reload = async () => {
    isLoading.value = true
    error.value = ""

    try {
      model.value = await resolveSurfaceData(toValue(surfaceIdSource), toValue(routeSource))
    } catch {
      model.value = {
        metrics: [],
        sections: [],
        note: `Live ${getAppSurfaceConfig(toValue(surfaceIdSource)).title.toLowerCase()} data is temporarily unavailable.`
      }
      error.value = "Live surface data is temporarily unavailable."
    } finally {
      isLoading.value = false
    }
  }

  watch(
    () => [toValue(surfaceIdSource), toValue(routeSource).fullPath, currentUser.value?.id] as const,
    () => {
      void reload()
    },
    {immediate: true}
  )

  return {
    model: computed(() => model.value),
    isLoading: computed(() => isLoading.value),
    error: computed(() => error.value),
    reload
  }
}
