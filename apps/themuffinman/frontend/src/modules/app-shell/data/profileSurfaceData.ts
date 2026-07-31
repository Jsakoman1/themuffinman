import type {AppSurfaceId} from "../shellDefinitions.ts"
import type {ShellSurfaceViewModel} from "../shellSurfaceData.ts"
import {loadProfileData} from "../shellSurfaceData.ts"
export const loadProfileSurfaceData = (surfaceId: AppSurfaceId): Promise<ShellSurfaceViewModel> => loadProfileData(surfaceId)
