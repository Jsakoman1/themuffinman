import {api, withAuth} from "../../../api/httpClient.ts"

export type BusinessProfile = {
  id: number
  ownerId: number
  ownerUsername: string
  businessName: string
  slug: string
  headline?: string | null
  description?: string | null
  contactEmail?: string | null
  contactPhone?: string | null
  websiteUrl?: string | null
  active: boolean
  createdAt?: string | null
  updatedAt?: string | null
}

export type BusinessProfileRequest = {
  businessName: string
  slug?: string | null
  headline?: string | null
  description?: string | null
  contactEmail?: string | null
  contactPhone?: string | null
  websiteUrl?: string | null
  active?: boolean | null
}

export type BusinessProfileListResponse = {
  items: BusinessProfile[]
}

export const businessApi = {
  async getDirectory(): Promise<BusinessProfileListResponse> {
    return (await api.get<BusinessProfileListResponse>("/business/profiles", withAuth())).data
  },

  async getProfile(slug: string): Promise<BusinessProfile> {
    return (await api.get<BusinessProfile>(`/business/profiles/${slug}`, withAuth())).data
  },

  async getMyProfile(): Promise<BusinessProfile | null> {
    return (await api.get<BusinessProfile | null>("/business/profiles/me", withAuth())).data
  },

  async saveMyProfile(dto: BusinessProfileRequest): Promise<BusinessProfile> {
    return (await api.put<BusinessProfile>("/business/profiles/me", dto, withAuth())).data
  }
}
