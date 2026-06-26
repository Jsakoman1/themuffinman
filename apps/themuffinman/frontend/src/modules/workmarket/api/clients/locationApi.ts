import {api, withAuth} from "../../../../api/httpClient.ts"
import type {LocationDebugStatus, LocationLookupCandidate, LocationLookupRequest, LocationLookupResponse, LocationReverseLookupRequest} from "../contracts.ts"

export const locationApi = {
  async lookupLocation(dto: LocationLookupRequest): Promise<LocationLookupResponse> {
    return (await api.post<LocationLookupResponse>("/location/lookup", dto, withAuth())).data
  },

  async reverseLookupLocation(dto: LocationReverseLookupRequest): Promise<LocationLookupCandidate> {
    return (await api.post<LocationLookupCandidate>("/location/reverse-lookup", dto, withAuth())).data
  },

  async getLocationDebugStatus(): Promise<LocationDebugStatus> {
    return (await api.get<LocationDebugStatus>("/location/admin/status", withAuth())).data
  }
}
