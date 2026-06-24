export const closeAfterDelay = (close: () => void, delayMs = 900) => {
  window.setTimeout(() => {
    close()
  }, delayMs)
}

