import {api, withAuth} from "../../../../api/httpClient.ts"
import type {DashboardVoiceConfig} from "../contracts.ts"

export type DashboardVoiceTranscription = {
  text: string
  provider: string
  model: string
}

export const dashboardApi = {
  async getDashboardVoiceConfig(): Promise<DashboardVoiceConfig> {
    return (await api.get<DashboardVoiceConfig>("/dashboard/me/voice-config", withAuth())).data
  },

  async transcribeVoiceAudio(audio: Blob): Promise<DashboardVoiceTranscription> {
    const formData = new FormData()
    const filename = audio.type.includes("mp4")
      ? "voice.mp4"
      : audio.type.includes("aac")
        ? "voice.aac"
        : "voice.webm"
    formData.append("audio", audio, filename)

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
  }
}
