import {
  IMAGE_COMPRESS_ERROR_MESSAGE,
  IMAGE_FILE_REQUIRED_MESSAGE,
  IMAGE_LOAD_ERROR_MESSAGE,
  IMAGE_PREPARE_ERROR_MESSAGE,
  IMAGE_READ_ERROR_MESSAGE
} from "./clientMessages.ts"

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

export const compressProfileAvatar = async (file: File, maxSize = 320, quality = 0.82) => {
  return compressImageFile(file, maxSize, quality)
}

export const compressImageFile = async (file: File, maxSize = 320, quality = 0.82) => {
  if (!file.type.startsWith("image/")) {
    throw new Error(IMAGE_FILE_REQUIRED_MESSAGE)
  }

  const objectUrl = URL.createObjectURL(file)

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
  } finally {
    URL.revokeObjectURL(objectUrl)
  }
}
