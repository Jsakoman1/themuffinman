import {api, withAuth} from "../../../api/httpClient.ts"
import type {ThingBorrowRequestResponseDTO, ThingListingListResponseDTO, ThingListingRequestDTO, ThingListingResponseDTO, ThingPreview} from "../../../contracts/index.ts"

export type ThingWishlistItem = {id?: number; listingId: number; title: string; ownerUsername?: string; sharedCircleIds: number[]; savedAt?: string}

export const thingsApi = {
  async getWishlist(): Promise<ThingWishlistItem[]> {
    return (await api.get<ThingWishlistItem[]>("/things/wishlist/me", withAuth())).data
  },
  async saveWishlist(item: ThingWishlistItem): Promise<ThingWishlistItem> {
    return (await api.put<ThingWishlistItem>(`/things/wishlist/me/${item.listingId}`, {sharedCircleIds: item.sharedCircleIds}, withAuth())).data
  },
  async removeWishlist(listingId: number): Promise<void> {
    await api.delete(`/things/wishlist/me/${listingId}`, withAuth())
  },
  async getListingsForScope(scope: "discover" | "mine", query = ""): Promise<ThingListingListResponseDTO> { return scope === "mine" ? thingsApi.getMyListings() : thingsApi.getListings(query) },
  async getListings(query = ""): Promise<ThingListingListResponseDTO> { return (await api.get<ThingListingListResponseDTO>("/things/listings", {params: {q: query || undefined}, ...withAuth()})).data },
  async getMyListings(): Promise<ThingListingListResponseDTO> { return (await api.get<ThingListingListResponseDTO>("/things/listings/me", withAuth())).data },
  async getListing(id: number): Promise<ThingListingResponseDTO> { return (await api.get<ThingListingResponseDTO>(`/things/listings/${id}`, withAuth())).data },
  async getPreview(id: number): Promise<ThingPreview> { return (await api.get<ThingPreview>(`/things/listings/${id}/preview`, withAuth())).data },
  async createListing(request: ThingListingRequestDTO): Promise<ThingListingResponseDTO> { return (await api.post<ThingListingResponseDTO>("/things/listings", request, withAuth())).data },
  async createQuickListing(title: string, description = ""): Promise<ThingListingResponseDTO> {
    return thingsApi.createListing({title: title.trim(), description: description.trim(), conditionNote: "", available: true})
  },
  async updateListing(id: number, request: ThingListingRequestDTO): Promise<ThingListingResponseDTO> { return (await api.put<ThingListingResponseDTO>(`/things/listings/${id}`, request, withAuth())).data },
  async archiveListing(id: number): Promise<void> { await api.delete(`/things/listings/${id}`, withAuth()) },
  async requestBorrow(id: number, message: string): Promise<ThingBorrowRequestResponseDTO> { return (await api.post<ThingBorrowRequestResponseDTO>(`/things/listings/${id}/borrow-requests`, {message}, withAuth())).data },
  async cancelBorrow(id: number): Promise<ThingBorrowRequestResponseDTO> { return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${id}/cancel`, undefined, withAuth())).data },
  async getMyBorrowRequests(): Promise<ThingBorrowRequestResponseDTO[]> { return (await api.get<ThingBorrowRequestResponseDTO[]>("/things/borrow-requests/me", withAuth())).data },
  async getOwnerBorrowRequests(): Promise<ThingBorrowRequestResponseDTO[]> { return (await api.get<ThingBorrowRequestResponseDTO[]>("/things/listings/me/borrow-requests", withAuth())).data },
  async decideBorrow(id: number, approve: boolean): Promise<ThingBorrowRequestResponseDTO> { return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${id}/decision`, undefined, {...withAuth(), params: {approve}})).data },
  async returnBorrow(id: number): Promise<ThingBorrowRequestResponseDTO> { return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${id}/return`, undefined, withAuth())).data }
}
