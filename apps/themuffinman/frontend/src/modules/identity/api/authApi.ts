import {api, withAuth} from "../../../api/httpClient.ts"
import type {AuthResponse, LoginRequest, RegisterRequest} from "../../../contracts/index.ts"

export type {AuthResponse, LoginRequest, RegisterRequest} from "../../../contracts/index.ts"

export const authApi = {
  async login(dto: LoginRequest): Promise<AuthResponse> {
    return (await api.post<AuthResponse>("/auth/login", dto)).data
  },

  async register(dto: RegisterRequest): Promise<AuthResponse> {
    return (await api.post<AuthResponse>("/auth/register", dto)).data
  },

  async me(): Promise<AuthResponse> {
    return (await api.get<AuthResponse>("/auth/me", withAuth())).data
  }
}
