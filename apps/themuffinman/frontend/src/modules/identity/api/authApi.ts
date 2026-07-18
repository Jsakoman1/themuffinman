import {api, withAuth} from "../../../api/httpClient.ts"
import type {AuthResponse, LoginRequest, PasswordRecoveryRequest, PasswordRecoveryResponse, PasswordResetRequest, RegisterRequest} from "../../../contracts/index.ts"

export type {AuthResponse, LoginRequest, PasswordRecoveryRequest, PasswordRecoveryResponse, PasswordResetRequest, RegisterRequest} from "../../../contracts/index.ts"

export const authApi = {
  async login(dto: LoginRequest): Promise<AuthResponse> {
    return (await api.post<AuthResponse>("/auth/login", dto)).data
  },

  async register(dto: RegisterRequest): Promise<AuthResponse> {
    return (await api.post<AuthResponse>("/auth/register", dto)).data
  },

  async requestPasswordRecovery(dto: PasswordRecoveryRequest): Promise<PasswordRecoveryResponse> {
    return (await api.post<PasswordRecoveryResponse>("/auth/password-recovery", dto)).data
  },

  async resetPassword(dto: PasswordResetRequest): Promise<void> {
    await api.post("/auth/password-reset", dto)
  },

  async me(): Promise<AuthResponse> {
    return (await api.get<AuthResponse>("/auth/me", withAuth())).data
  }
}
