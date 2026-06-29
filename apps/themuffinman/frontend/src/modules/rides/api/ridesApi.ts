import {api, withAuth} from "../../../api/httpClient.ts"

export type RideOffer = {
  id: number
  driverId: number
  driverUsername: string
  origin: string
  destination: string
  departureAt: string
  seats: number
  note?: string | null
  active: boolean
  visibleCircleNames: string[]
}

export type RideOfferRequest = {
  origin: string
  destination: string
  departureAt: string
  seats: number
  note?: string | null
  active?: boolean | null
  visibleCircleIds?: number[]
}

export type RideOfferListResponse = {
  items: RideOffer[]
}

export const ridesApi = {
  async getVisibleOffers(): Promise<RideOfferListResponse> {
    return (await api.get<RideOfferListResponse>("/rides/offers", withAuth())).data
  },

  async getMyOffers(): Promise<RideOfferListResponse> {
    return (await api.get<RideOfferListResponse>("/rides/offers/me", withAuth())).data
  },

  async createOffer(dto: RideOfferRequest): Promise<RideOffer> {
    return (await api.post<RideOffer>("/rides/offers", dto, withAuth())).data
  }
}
