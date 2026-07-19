import type {
  VisionConversationListResponseDTO,
  VisionConversationTurnRequestDTO,
  VisionConversationTurnResponseDTO
} from "../../../contracts/index.ts"
import type {
  VisionConversationListResponse,
  VisionConversationTurnResponse
} from "./visionConversationViewModels.ts"

export type VisionConversationTurnTransportRequest = VisionConversationTurnRequestDTO
export type VisionConversationTurnTransportResponse = VisionConversationTurnResponseDTO
export type VisionConversationListTransportResponse = VisionConversationListResponseDTO

/**
 * The generated DTO is the wire contract. The view model keeps nullable values and
 * UI-only discriminants explicit until the backend schema can express those details.
 */
export const toVisionConversationViewModel = (response: VisionConversationTurnTransportResponse): VisionConversationTurnResponse => response as unknown as VisionConversationTurnResponse
export const toVisionConversationListViewModel = (response: VisionConversationListTransportResponse): VisionConversationListResponse => response as unknown as VisionConversationListResponse
