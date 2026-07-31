import type {ShellSurfaceViewModel} from "../shellSurfaceData.ts"
import {loadCalendarData} from "../shellSurfaceData.ts"
export const loadCalendarSurfaceData = (): Promise<ShellSurfaceViewModel> => loadCalendarData()
