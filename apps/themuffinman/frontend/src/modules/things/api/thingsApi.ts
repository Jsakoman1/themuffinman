import {api, withAuth} from "../../../api/httpClient.ts"
import type {ThingBorrowRequestResponseDTO, ThingListingListResponseDTO, ThingListingRequestDTO, ThingListingResponseDTO, ThingPreview} from "../../../contracts/index.ts"

export const thingsApi = {
  async getListings(): Promise<ThingListingListResponseDTO> { return (await api.get<ThingListingListResponseDTO>("/things/listings", withAuth())).data },
  async getMyListings(): Promise<ThingListingListResponseDTO> { return (await api.get<ThingListingListResponseDTO>("/things/listings/me", withAuth())).data },
  async getListing(id: number): Promise<ThingListingResponseDTO> { return (await api.get<ThingListingResponseDTO>(`/things/listings/${id}`, withAuth())).data },
  async getPreview(id: number): Promise<ThingPreview> { return (await api.get<ThingPreview>(`/things/listings/${id}/preview`, withAuth())).data },
  async createListing(request: ThingListingRequestDTO): Promise<ThingListingResponseDTO> { return (await api.post<ThingListingResponseDTO>("/things/listings", request, withAuth())).data },
  async updateListing(id: number, request: ThingListingRequestDTO): Promise<ThingListingResponseDTO> { return (await api.put<ThingListingResponseDTO>(`/things/listings/${id}`, request, withAuth())).data },
  async archiveListing(id: number): Promise<void> { await api.delete(`/things/listings/${id}`, withAuth()) },
  async requestBorrow(id: number, message: string): Promise<ThingBorrowRequestResponseDTO> { return (await api.post<ThingBorrowRequestResponseDTO>(`/things/listings/${id}/borrow-requests`, {message}, withAuth())).data },
  async cancelBorrow(id: number): Promise<ThingBorrowRequestResponseDTO> { return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${id}/cancel`, undefined, withAuth())).data },
  async getMyBorrowRequests(): Promise<ThingBorrowRequestResponseDTO[]> { return (await api.get<ThingBorrowRequestResponseDTO[]>("/things/borrow-requests/me", withAuth())).data },
  async getOwnerBorrowRequests(): Promise<ThingBorrowRequestResponseDTO[]> { return (await api.get<ThingBorrowRequestResponseDTO[]>("/things/listings/me/borrow-requests", withAuth())).data },
  async decideBorrow(id: number, approve: boolean): Promise<ThingBorrowRequestResponseDTO> { return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${id}/decision`, undefined, {...withAuth(), params: {approve}})).data },
  async returnBorrow(id: number): Promise<ThingBorrowRequestResponseDTO> { return (await api.patch<ThingBorrowRequestResponseDTO>(`/things/borrow-requests/${id}/return`, undefined, withAuth())).data }
}
