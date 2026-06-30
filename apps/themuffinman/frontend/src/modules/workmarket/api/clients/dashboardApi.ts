import {api, withAuth} from "../../../../api/httpClient.ts"
import type {DashboardResponse, DashboardSummary, DashboardVoiceConfig} from "../contracts.ts"

export type DashboardVoiceTranscription = {
  text: string
  provider: string
  model: string
}

export type DashboardVisionPromptResponse = {
  prompt: string
  normalizedPrompt: string
  source: string
  translationProvider: string
  translationApplied: boolean
  translationReliable: boolean
  activeFilter: "best-match" | "today" | "nearby"
  surfaceMode: "browse" | "compare" | "focus"
  assistantNote: string
  matchedSignals: string[]
}

export const dashboardApi = {
  async getDashboardSummary(): Promise<DashboardSummary> {
    return (await api.get<DashboardSummary>("/dashboard/me/summary", withAuth())).data
  },

  async getDashboard(): Promise<DashboardResponse> {
    return (await api.get<DashboardResponse>("/dashboard/me", withAuth())).data
  },

  async getDashboardVoiceConfig(): Promise<DashboardVoiceConfig> {
    return (await api.get<DashboardVoiceConfig>("/dashboard/me/voice-config", withAuth())).data
  },

  async transcribeVoiceAudio(audio: Blob): Promise<DashboardVoiceTranscription> {
    const formData = new FormData()
    formData.append("audio", audio, "voice.webm")

    const auth = withAuth()
    return (await api.post<DashboardVoiceTranscription>("/dashboard/me/voice/transcribe", formData, {
      ...auth
    })).data
  },

  async speakVoiceText(text: string): Promise<ArrayBuffer> {
    const auth = withAuth()
    return (await api.post<ArrayBuffer>("/dashboard/me/voice/speak", {text}, {
      ...auth,
      responseType: "arraybuffer"
    })).data
  },

  async processVisionPrompt(prompt: string, source = "text"): Promise<DashboardVisionPromptResponse> {
    const auth = withAuth()
    return (await api.post<DashboardVisionPromptResponse>("/dashboard/me/vision/prompt", {prompt, source}, auth)).data
  }
}
