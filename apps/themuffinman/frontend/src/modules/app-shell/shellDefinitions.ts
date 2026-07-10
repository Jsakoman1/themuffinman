import type {RouteLocationRaw} from "vue-router"
import {
  buildAppPrimaryNavItems,
  topLevelNavigationPromotionPolicy
} from "./shellRouteRegistry.ts"

export type AppPrimaryNavId =
  | "home"
  | "work"
  | "chat"
  | "calendar"
  | "business"
  | "circles"
  | "profile"

export type AppSurfaceId =
  | "home"
  | "work"
  | "work-quests"
  | "work-applications"
  | "chat"
  | "chat-conversation"
  | "calendar"
  | "business"
  | "business-profile"
  | "business-bookings"
  | "business-calendar"
  | "circles"
  | "profile"
  | "profile-settings"

export type AppSurfaceAction = {
  label: string
  description: string
  to: RouteLocationRaw
  tone?: "primary" | "secondary" | "vision"
}

export type AppSurfaceSection = {
  title: string
  description: string
  items: string[]
}

export type AppSurfaceConfig = {
  id: AppSurfaceId
  navId: AppPrimaryNavId
  eyebrow: string
  title: string
  description: string
  supportingText: string
  sections: AppSurfaceSection[]
  actions: AppSurfaceAction[]
}

export type AppPrimaryNavItem = {
  id: AppPrimaryNavId
  label: string
  description: string
  to: RouteLocationRaw
}

export {topLevelNavigationPromotionPolicy}

const visionRoute = (prompt?: string): RouteLocationRaw => {
  if (!prompt) {
    return {path: "/vision"}
  }

  return {
    path: "/vision",
    query: {
      prompt,
      autorun: "1"
    }
  }
}

export const appPrimaryNavItems: AppPrimaryNavItem[] = buildAppPrimaryNavItems()

const appSurfaceConfigs: Record<AppSurfaceId, AppSurfaceConfig> = {
  home: {
    id: "home",
    navId: "home",
    eyebrow: "Shared Utility",
    title: "One calm place to start.",
    description: "Use the shell for orientation. Use Vision when the task becomes guided, semantic, or cross-module.",
    supportingText: "Home is intentionally light. It should help you pick the next move without pretending to own every detail view.",
    sections: [
      {
        title: "Continue",
        description: "Re-enter the areas that usually matter first.",
        items: [
          "Review your active work, bookings, and applications.",
          "Jump back into the latest conversation.",
          "Open business bookings or calendar when time matters."
        ]
      },
      {
        title: "Attention",
        description: "The shell stays quiet, but it should still make important lanes visible.",
        items: [
          "Work and application state stays under Work.",
          "Business booking and schedule state stays under Business.",
          "Conversations stay under Chat.",
          "Time-based coordination stays under Calendar and Business."
        ]
      },
      {
        title: "Use Vision",
        description: "Vision remains the guided deep-work surface.",
        items: [
          "Find work near me tomorrow evening.",
          "Open my circles and help me write a request.",
          "Summarize what needs attention today."
        ]
      }
    ],
    actions: [
      {
        label: "Open Work",
        description: "Browse work, quests, and applications.",
        to: {path: "/work"},
        tone: "primary"
      },
      {
        label: "Open Chat",
        description: "Go straight to the conversation workspace.",
        to: {path: "/chat"},
        tone: "secondary"
      },
      {
        label: "Open Business",
        description: "Review bookings, appointments, and availability.",
        to: {path: "/business"},
        tone: "secondary"
      },
      {
        label: "Ask Vision",
        description: "Start guided help from a blank-canvas route.",
        to: visionRoute(),
        tone: "vision"
      }
    ]
  },
  work: {
    id: "work",
    navId: "work",
    eyebrow: "Work",
    title: "Browse work without losing the guided path.",
    description: "Work is the stable entry for discover, my quests, and applications. Guided create, update, and apply flows still escalate into Vision.",
    supportingText: "Phase one keeps detail continuity Vision-native: quest detail and application detail remain canonical under `/vision` routes.",
    sections: [
      {
        title: "Discover",
        description: "Stable browse space for available work.",
        items: [
          "Ranked discovery and filters live here.",
          "Quest detail remains Vision-native in phase one.",
          "Use Vision when you want guided search or creation."
        ]
      },
      {
        title: "My quests",
        description: "Your owned work should stay easy to scan.",
        items: [
          "Quest status and next actions belong here.",
          "Escalate into Vision for guided create or review work.",
          "Do not introduce a second quest-detail stack."
        ]
      },
      {
        title: "Applications",
        description: "Applications stay nested under Work, not top-level nav.",
        items: [
          "Applications list is stable and deterministic.",
          "Application detail remains Vision-native in phase one.",
          "Guided update and withdraw flows escalate into Vision."
        ]
      }
    ],
    actions: [
      {
        label: "My quests",
        description: "Open the quest-oriented view.",
        to: {path: "/work/quests"},
        tone: "secondary"
      },
      {
        label: "Applications",
        description: "Open your application-focused view.",
        to: {path: "/work/applications"},
        tone: "secondary"
      },
      {
        label: "Create with Vision",
        description: "Use guided quest creation.",
        to: visionRoute("create quest"),
        tone: "vision"
      }
    ]
  },
  "work-quests": {
    id: "work-quests",
    navId: "work",
    eyebrow: "Work / My Quests",
    title: "Quest ownership stays in Work entry space.",
    description: "This is the stable list entry. Canonical detail ownership remains Vision-native in phase one.",
    supportingText: "If you need guided create, review, or correction, jump into Vision instead of opening a second detail system.",
    sections: [
      {
        title: "Phase-one rule",
        description: "Do not create parallel detail ownership under Work.",
        items: [
          "Quest detail routes remain canonical under `/vision/quests/:id`.",
          "List browsing stays deterministic.",
          "Guided mutation stays Vision-first."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Work",
        description: "Return to the main work entry surface.",
        to: {path: "/work"},
        tone: "secondary"
      },
      {
        label: "Open a quest in Vision",
        description: "Use the canonical detail route pattern.",
        to: visionRoute("show quest #42"),
        tone: "vision"
      }
    ]
  },
  "work-applications": {
    id: "work-applications",
    navId: "work",
    eyebrow: "Work / Applications",
    title: "Applications stay nested under Work.",
    description: "Use this route as the stable applications entry. Canonical detail ownership remains Vision-native in phase one.",
    supportingText: "This keeps top-level navigation compact while preserving guided application flows in Vision.",
    sections: [
      {
        title: "Phase-one rule",
        description: "Application detail remains canonical under Vision.",
        items: [
          "Do not add `/work/applications/:id` yet.",
          "Guided update and withdraw flows stay Vision-owned.",
          "Work continues to own deterministic applications browse."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Work",
        description: "Return to the main work entry surface.",
        to: {path: "/work"},
        tone: "secondary"
      },
      {
        label: "Open an application in Vision",
        description: "Use the canonical detail route pattern.",
        to: visionRoute("show application #42"),
        tone: "vision"
      }
    ]
  },
  chat: {
    id: "chat",
    navId: "chat",
    eyebrow: "Chat",
    title: "Stable workspace for inbox and threads.",
    description: "Chat owns the deterministic conversation workspace. Vision still handles semantic target resolution and guided open-chat flows.",
    supportingText: "This dual ownership is intentional: browse in Chat, escalate to Vision when the task starts from intent instead of a known thread.",
    sections: [
      {
        title: "Workspace ownership",
        description: "Chat is the canonical place for thread browsing.",
        items: [
          "Inbox and thread reading belong here.",
          "Known conversation targets stay in module space.",
          "Unknown or semantic target resolution can start in Vision."
        ]
      }
    ],
    actions: [
      {
        label: "Open a thread",
        description: "Use the stable chat workspace path.",
        to: {path: "/chat/1"},
        tone: "secondary"
      },
      {
        label: "Ask Vision to open chat",
        description: "Use semantic target resolution.",
        to: visionRoute("show chat"),
        tone: "vision"
      }
    ]
  },
  "chat-conversation": {
    id: "chat-conversation",
    navId: "chat",
    eyebrow: "Chat / Conversation",
    title: "Thread detail stays chat-owned.",
    description: "Known thread browsing belongs in Chat. Vision remains available when the user starts from intent instead of a known counterpart.",
    supportingText: "This placeholder route proves canonical chat workspace ownership without introducing semantic duplication in the shell.",
    sections: [
      {
        title: "Current thread",
        description: "A stable thread route belongs to Chat, not to the shell home or Work.",
        items: [
          "Known thread -> stay in Chat.",
          "Need semantic outreach -> escalate into Vision.",
          "Preserve route ownership and keep transitions deliberate."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Chat",
        description: "Return to the chat workspace.",
        to: {path: "/chat"},
        tone: "secondary"
      },
      {
        label: "Ask Vision for help",
        description: "Open guided chat help.",
        to: visionRoute("show chat"),
        tone: "vision"
      }
    ]
  },
  calendar: {
    id: "calendar",
    navId: "calendar",
    eyebrow: "Calendar",
    title: "Time is an index, not the detail owner.",
    description: "Calendar helps users coordinate across work and business, but it should route out to the owning quest or booking surface for details.",
    supportingText: "This keeps time visible without letting calendar pages absorb business rules or entity detail ownership.",
    sections: [
      {
        title: "Coordination lens",
        description: "Calendar is where time pressure and sequencing become visible.",
        items: [
          "Quest, booking, and conversation detail stays in the owning surface.",
          "Use Vision for guided planning or scheduling help.",
          "Keep calendar quiet and operational."
        ]
      }
    ],
    actions: [
      {
        label: "Business calendar",
        description: "Open the owner-oriented business calendar.",
        to: {path: "/business/calendar"},
        tone: "secondary"
      },
      {
        label: "Ask Vision to plan",
        description: "Use guided time-based help.",
        to: visionRoute("show calendar"),
        tone: "vision"
      }
    ]
  },
  business: {
    id: "business",
    navId: "business",
    eyebrow: "Business",
    title: "Owner operations need their own stable lane.",
    description: "Business is distinct from Work. It owns profile, bookings, and owner calendar flows without pretending to be a generic dashboard.",
    supportingText: "Guided business help may still escalate into Vision, but operational ownership stays in the business route family.",
    sections: [
      {
        title: "Owner lanes",
        description: "Keep profile, bookings, and calendar easy to reach.",
        items: [
          "Business profile is its own owner surface.",
          "Bookings and calendar remain operational.",
          "Guided booking assistance can escalate into Vision."
        ]
      }
    ],
    actions: [
      {
        label: "Profile",
        description: "Open the business profile surface.",
        to: {path: "/business/profile"},
        tone: "secondary"
      },
      {
        label: "Bookings",
        description: "Open the business bookings surface.",
        to: {path: "/business/bookings"},
        tone: "secondary"
      },
      {
        label: "Calendar",
        description: "Open the business calendar surface.",
        to: {path: "/business/calendar"},
        tone: "secondary"
      }
    ]
  },
  "business-profile": {
    id: "business-profile",
    navId: "business",
    eyebrow: "Business / Profile",
    title: "Business identity stays business-owned.",
    description: "Keep the owner profile in module space. Use Vision only when the task becomes guided or semantic.",
    supportingText: "This surface exists so Business does not collapse into Work or Profile semantics.",
    sections: [
      {
        title: "Identity surface",
        description: "This route should stay owner-operational.",
        items: [
          "Business profile belongs to Business.",
          "Do not reframe it as a Work surface.",
          "Escalate only when guided assistance helps."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Business",
        description: "Return to business overview.",
        to: {path: "/business"},
        tone: "secondary"
      },
      {
        label: "Ask Vision",
        description: "Use guided help from the Vision surface.",
        to: visionRoute("show business profile"),
        tone: "vision"
      }
    ]
  },
  "business-bookings": {
    id: "business-bookings",
    navId: "business",
    eyebrow: "Business / Bookings",
    title: "Bookings stay operational and route-owned.",
    description: "Bookings belong to the business route family. Calendar and Vision may support them, but should not own them.",
    supportingText: "This protects owner flows from becoming hidden inside generic shell widgets.",
    sections: [
      {
        title: "Operational bookings",
        description: "Booking ownership should stay clear.",
        items: [
          "Bookings are not a Home detail surface.",
          "Bookings are not a Calendar detail surface.",
          "Bookings may escalate into Vision for guided help."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Business",
        description: "Return to business overview.",
        to: {path: "/business"},
        tone: "secondary"
      },
      {
        label: "Open calendar",
        description: "Move to the business calendar lens.",
        to: {path: "/business/calendar"},
        tone: "secondary"
      }
    ]
  },
  "business-calendar": {
    id: "business-calendar",
    navId: "business",
    eyebrow: "Business / Calendar",
    title: "Owner calendar stays a business route.",
    description: "The owner calendar is a stable operational read surface. Use Vision when the task becomes guided or explanatory.",
    supportingText: "This route proves that time-based business work can stay module-owned without collapsing back into generic shell chrome.",
    sections: [
      {
        title: "Owner calendar",
        description: "Time stays visible without becoming the detail owner.",
        items: [
          "Calendar routes to the owning booking or business context.",
          "Guided planning can still escalate to Vision.",
          "Keep the main shell quiet."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Business",
        description: "Return to business overview.",
        to: {path: "/business"},
        tone: "secondary"
      },
      {
        label: "Ask Vision to plan",
        description: "Use guided planning help.",
        to: visionRoute("show business calendar"),
        tone: "vision"
      }
    ]
  },
  circles: {
    id: "circles",
    navId: "circles",
    eyebrow: "Circles",
    title: "Trust and visibility need a stable entry lane.",
    description: "Browse circles and requests in module space. Escalate into Vision when the action becomes create, request, rename, delete, or person-resolution heavy.",
    supportingText: "This keeps circles legible without duplicating guided social flows in the shell.",
    sections: [
      {
        title: "Module-owned browse",
        description: "Stable overview first, guided action second.",
        items: [
          "Browse circles and requests here.",
          "Use Vision for create, request, rename, delete, or outreach flows.",
          "Preserve existing Vision-native guided continuity."
        ]
      }
    ],
    actions: [
      {
        label: "Ask Vision to show circles",
        description: "Open the guided continuity path.",
        to: visionRoute("show circles"),
        tone: "vision"
      }
    ]
  },
  profile: {
    id: "profile",
    navId: "profile",
    eyebrow: "Profile",
    title: "Self-facing information stays calm and stable.",
    description: "Profile is the deterministic lane for self view and settings entry. Guided self mutation and semantic help may still escalate into Vision.",
    supportingText: "This split keeps profile readable while preserving the adaptive route for guided edits and continuity.",
    sections: [
      {
        title: "Stable self view",
        description: "Use module space when the user is reviewing steady self-facing information.",
        items: [
          "Profile remains a primary destination.",
          "Settings stays nested under Profile.",
          "Use Vision when the task becomes guided or semantic."
        ]
      }
    ],
    actions: [
      {
        label: "Settings",
        description: "Open nested settings entry.",
        to: {path: "/profile/settings"},
        tone: "secondary"
      },
      {
        label: "Ask Vision to show profile",
        description: "Open the adaptive profile continuation path.",
        to: visionRoute("show profile"),
        tone: "vision"
      }
    ]
  },
  "profile-settings": {
    id: "profile-settings",
    navId: "profile",
    eyebrow: "Profile / Settings",
    title: "Settings stay nested and quiet.",
    description: "Settings should not become top-level navigation. Keep them close to Profile and escalate to Vision only when guidance helps.",
    supportingText: "This keeps navigation compact and avoids fragmenting identity and preference work across too many top-level lanes.",
    sections: [
      {
        title: "Nested settings",
        description: "Stay deterministic when the task is simple.",
        items: [
          "Settings belong under Profile.",
          "Use Vision when the task needs guided help.",
          "Do not promote Settings into top-level nav."
        ]
      }
    ],
    actions: [
      {
        label: "Back to Profile",
        description: "Return to the profile entry surface.",
        to: {path: "/profile"},
        tone: "secondary"
      },
      {
        label: "Ask Vision",
        description: "Open guided settings continuity.",
        to: visionRoute("show settings"),
        tone: "vision"
      }
    ]
  }
}

export const getAppSurfaceConfig = (surfaceId: AppSurfaceId) => appSurfaceConfigs[surfaceId]
