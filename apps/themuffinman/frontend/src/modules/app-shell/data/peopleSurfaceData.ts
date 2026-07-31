import type {ShellSurfaceViewModel} from "../shellSurfaceData.ts"
import {loadCirclesData} from "../shellSurfaceData.ts"
export const loadPeopleSurfaceData = (): Promise<ShellSurfaceViewModel> => loadCirclesData()
