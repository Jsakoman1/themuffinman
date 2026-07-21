import {api, withAuth} from "../../../api/httpClient.ts"
import type {RideOfferListResponseDTO, RideOfferRequestDTO, RideOfferResponseDTO} from "../../../contracts/index.ts"

export type CommutePreference = {
  id: number | null
  enabled: boolean
  consentGranted: boolean
  homeArea: string | null
  workArea: string | null
  weekdays: number[]
  departureTime: string | null
  returnTime: string | null
  updatedAt: string | null
}

export const ridesApi = {
  async getOffersForScope(scope: "discover" | "mine"): Promise<RideOfferListResponseDTO> { return scope === "mine" ? this.getMyOffers() : this.getOffers() },
  async getOffers(): Promise<RideOfferListResponseDTO> { return (await api.get<RideOfferListResponseDTO>("/rides/offers", withAuth())).data },
  async getOffer(id: number): Promise<RideOfferResponseDTO> { return (await api.get<RideOfferResponseDTO>(`/rides/offers/${id}`, withAuth())).data },
  async getMyOffers(): Promise<RideOfferListResponseDTO> { return (await api.get<RideOfferListResponseDTO>("/rides/offers/me", withAuth())).data },
  async findMatches(filters: {origin?: string; destination?: string; departureFrom?: string; departureTo?: string} = {}): Promise<RideOfferListResponseDTO> {
    return (await api.get<RideOfferListResponseDTO>("/rides/offers/matches", {params: {...filters, origin: filters.origin || undefined, destination: filters.destination || undefined}, ...withAuth()})).data
  },
  async getCommutePreference(): Promise<CommutePreference> { return (await api.get<CommutePreference>("/rides/commute/me", withAuth())).data },
  async updateCommutePreference(request: Omit<CommutePreference, "id" | "updatedAt">): Promise<CommutePreference> { return (await api.put<CommutePreference>("/rides/commute/me", request, withAuth())).data },
  async createOffer(request: RideOfferRequestDTO): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>("/rides/offers", request, withAuth())).data },
  async updateOffer(id: number, request: RideOfferRequestDTO): Promise<RideOfferResponseDTO> { return (await api.put<RideOfferResponseDTO>(`/rides/offers/${id}`, request, withAuth())).data },
  async join(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/join`, undefined, withAuth())).data },
  async leave(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/leave`, undefined, withAuth())).data },
  async cancel(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/cancel`, undefined, withAuth())).data },
  async start(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/start`, undefined, withAuth())).data },
  async complete(id: number): Promise<RideOfferResponseDTO> { return (await api.post<RideOfferResponseDTO>(`/rides/offers/${id}/complete`, undefined, withAuth())).data }
}
