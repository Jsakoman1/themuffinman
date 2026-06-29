import {api, withAuth} from "../../../api/httpClient.ts"

export type ThingListing = {
  id: number
  ownerId: number
  ownerUsername: string
  title: string
  description?: string | null
  conditionNote?: string | null
  available: boolean
  myPendingRequestId?: number | null
}

export type ThingListingRequest = {
  title: string
  description?: string | null
  conditionNote?: string | null
  available?: boolean | null
}

export type ThingBorrowRequest = {
  message?: string | null
}

export type ThingListingListResponse = {
  items: ThingListing[]
}

export const thingsApi = {
  async getAvailableListings(): Promise<ThingListingListResponse> {
    return (await api.get<ThingListingListResponse>("/things/listings", withAuth())).data
  },

  async getMyListings(): Promise<ThingListingListResponse> {
    return (await api.get<ThingListingListResponse>("/things/listings/me", withAuth())).data
  },

  async createListing(dto: ThingListingRequest): Promise<ThingListing> {
    return (await api.post<ThingListing>("/things/listings", dto, withAuth())).data
  },

  async requestBorrow(listingId: number, dto: ThingBorrowRequest) {
    return (await api.post(`/things/listings/${listingId}/borrow-requests`, dto, withAuth())).data
  }
}
