export type ProductModuleKey = "work" | "business" | "things" | "rides" | "chat" | "vision"

export type ProductModule = {
  key: ProductModuleKey
  title: string
  shortTitle: string
  path: string
  description: string
  state: "live" | "planned"
}

export const productModules: ProductModule[] = [
  {
    key: "work",
    title: "Work Marketplace",
    shortTitle: "Work",
    path: "/work",
    description: "Post jobs, find work, manage applications, and coordinate local paid help.",
    state: "live"
  },
  {
    key: "business",
    title: "Business Hub",
    shortTitle: "Business",
    path: "/business",
    description: "Business profiles, mini websites, calendars, and appointment booking.",
    state: "live"
  },
  {
    key: "things",
    title: "Thing Sharing",
    shortTitle: "Things",
    path: "/things",
    description: "Lend, borrow, and coordinate everyday item sharing with trusted people.",
    state: "live"
  },
  {
    key: "rides",
    title: "Car Sharing",
    shortTitle: "Rides",
    path: "/rides",
    description: "Coordinate voluntary route-based rides between selected circles.",
    state: "live"
  },
  {
    key: "chat",
    title: "Shared Chat",
    shortTitle: "Chat",
    path: "/chat",
    description: "Cross-module messaging that follows the same user through work, circles, and future modules.",
    state: "live"
  },
  {
    key: "vision",
    title: "Vision Surface",
    shortTitle: "Vision",
    path: "/vision",
    description: "Experimental long-term adaptive canvas for parallel voice, visual feedback, and curated task surfaces.",
    state: "live"
  }
]

export const getProductModule = (key: string | undefined | null) => {
  return productModules.find((module) => module.key === key) ?? null
}
