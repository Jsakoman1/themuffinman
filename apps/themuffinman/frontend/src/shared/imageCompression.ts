import {
  IMAGE_COMPRESS_ERROR_MESSAGE,
  IMAGE_FILE_REQUIRED_MESSAGE,
  IMAGE_LOAD_ERROR_MESSAGE,
  IMAGE_PREPARE_ERROR_MESSAGE,
  IMAGE_READ_ERROR_MESSAGE,
  QUEST_IMAGE_TOO_LARGE_MESSAGE
} from "./clientMessages.ts"

const QUEST_IMAGE_MAX_LENGTH = 350_000

const loadImage = (src: string) => {
  return new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image()

    image.onload = () => resolve(image)
    image.onerror = () => reject(new Error(IMAGE_LOAD_ERROR_MESSAGE))
    image.src = src
  })
}

const blobToDataUrl = (blob: Blob) => {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result
      if (typeof result !== "string") {
        reject(new Error(IMAGE_READ_ERROR_MESSAGE))
        return
      }

      resolve(result)
    }
    reader.onerror = () => reject(new Error(IMAGE_READ_ERROR_MESSAGE))
    reader.readAsDataURL(blob)
  })
}

const fileToDataUrl = (file: File) => {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result
      if (typeof result !== "string") {
        reject(new Error(IMAGE_READ_ERROR_MESSAGE))
        return
      }

      resolve(result)
    }
    reader.onerror = () => reject(new Error(IMAGE_READ_ERROR_MESSAGE))
    reader.readAsDataURL(file)
  })
}

export const compressProfileAvatar = async (file: File, maxSize = 320, quality = 0.82) => {
  return compressImageFile(file, maxSize, quality)
}

export const compressImageFile = async (file: File, maxSize = 320, quality = 0.82) => {
  if (!file.type.startsWith("image/")) {
    throw new Error(IMAGE_FILE_REQUIRED_MESSAGE)
  }

  const objectUrl = URL.createObjectURL(file)

  try {
    try {
      const image = await loadImage(objectUrl)
      const scale = Math.min(1, maxSize / Math.max(image.width, image.height))
      const width = Math.max(1, Math.round(image.width * scale))
      const height = Math.max(1, Math.round(image.height * scale))

      const canvas = document.createElement("canvas")
      canvas.width = width
      canvas.height = height

      const context = canvas.getContext("2d")
      if (!context) {
        throw new Error(IMAGE_PREPARE_ERROR_MESSAGE)
      }

      context.fillStyle = "#ffffff"
      context.fillRect(0, 0, width, height)
      context.drawImage(image, 0, 0, width, height)

      const blob = await new Promise<Blob>((resolve, reject) => {
        canvas.toBlob((value) => {
          if (!value) {
            reject(new Error(IMAGE_COMPRESS_ERROR_MESSAGE))
            return
          }

          resolve(value)
        }, "image/jpeg", quality)
      })

      return await blobToDataUrl(blob)
    } catch {
      // Fallback keeps uploads working even if canvas/image decoding fails.
      return await fileToDataUrl(file)
    }
  } finally {
    URL.revokeObjectURL(objectUrl)
  }
}

export const compressQuestImageFile = async (file: File) => {
  const attempts: Array<{maxSize: number; quality: number}> = [
    {maxSize: 1400, quality: 0.84},
    {maxSize: 1200, quality: 0.78},
    {maxSize: 1000, quality: 0.72},
    {maxSize: 840, quality: 0.68},
    {maxSize: 720, quality: 0.62},
  ]

  for (const attempt of attempts) {
    const compressed = await compressImageFile(file, attempt.maxSize, attempt.quality)
    if (compressed.length <= QUEST_IMAGE_MAX_LENGTH) {
      return compressed
    }
  }

  throw new Error(QUEST_IMAGE_TOO_LARGE_MESSAGE)
}
