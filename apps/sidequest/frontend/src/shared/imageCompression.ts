const loadImage = (src: string) => {
  return new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image()

    image.onload = () => resolve(image)
    image.onerror = () => reject(new Error("Could not load image"))
    image.src = src
  })
}

const blobToDataUrl = (blob: Blob) => {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result
      if (typeof result !== "string") {
        reject(new Error("Could not read compressed image"))
        return
      }

      resolve(result)
    }
    reader.onerror = () => reject(new Error("Could not read compressed image"))
    reader.readAsDataURL(blob)
  })
}

export const compressProfileAvatar = async (file: File, maxSize = 320, quality = 0.82) => {
  return compressImageFile(file, maxSize, quality)
}

export const compressImageFile = async (file: File, maxSize = 320, quality = 0.82) => {
  if (!file.type.startsWith("image/")) {
    throw new Error("Please choose an image file.")
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
      throw new Error("Could not prepare image for upload")
    }

    context.fillStyle = "#ffffff"
    context.fillRect(0, 0, width, height)
    context.drawImage(image, 0, 0, width, height)

    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob((value) => {
        if (!value) {
          reject(new Error("Could not compress image"))
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
