import type {AppSurfaceId} from "../shellDefinitions.ts"
import type {ShellSurfaceViewModel} from "../shellSurfaceData.ts"
import {loadBusinessData} from "../shellSurfaceData.ts"
export const loadBusinessSurfaceData = (surfaceId: AppSurfaceId): Promise<ShellSurfaceViewModel> => loadBusinessData(surfaceId)
