const escapeHtml = (value: string) => {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#39;")
}

const safeUrl = (value: string) => {
  const trimmed = value.trim()
  if (!trimmed) {
    return ""
  }

  if (/^(https?:|mailto:|\/|#)/i.test(trimmed)) {
    return trimmed
  }

  return ""
}

const safeImageUrl = (value: string) => {
  const trimmed = value.trim()
  if (/^data:image\/[a-zA-Z0-9.+-]+;base64,/i.test(trimmed)) {
    return trimmed
  }

  return ""
}

const isHtmlLike = (value: string) => /<\/?[a-z][\s\S]*>/i.test(value)

const renderMarkdownLikeText = (value: string) => {
  const lines = value.split(/\r?\n/)
  const htmlParts: string[] = []
  const paragraphLines: string[] = []
  let inList = false

  const flushParagraph = () => {
    if (!paragraphLines.length) {
      return
    }

    htmlParts.push(`<p>${paragraphLines.map((line) => escapeHtml(line)).join(" ")}</p>`)
    paragraphLines.length = 0
  }

  const closeList = () => {
    if (!inList) {
      return
    }

    htmlParts.push("</ul>")
    inList = false
  }

  for (const line of lines) {
    const trimmed = line.trim()

    if (!trimmed) {
      flushParagraph()
      closeList()
      continue
    }

    const listMatch = /^[-*]\s+(.+)$/.exec(trimmed)
    if (listMatch) {
      flushParagraph()

      if (!inList) {
        htmlParts.push("<ul>")
        inList = true
      }

      htmlParts.push(`<li>${escapeHtml(listMatch[1])}</li>`)
      continue
    }

    closeList()
    paragraphLines.push(trimmed)
  }

  flushParagraph()
  closeList()

  return htmlParts.join("")
}

const sanitizeNode = (node: Node): string => {
  if (node.nodeType === Node.TEXT_NODE) {
    return escapeHtml(node.textContent ?? "")
  }

  if (node.nodeType !== Node.ELEMENT_NODE) {
    return ""
  }

  const element = node as HTMLElement
  const tag = element.tagName.toUpperCase()
  const children = Array.from(element.childNodes).map(sanitizeNode).join("")

  if (tag === "BR") {
    return "<br>"
  }

  if (tag === "A") {
    const href = safeUrl(element.getAttribute("href") ?? "")
    const linkHref = href || "#"
    return `<a href="${escapeHtml(linkHref)}" target="_blank" rel="noreferrer noopener">${children || escapeHtml(element.textContent ?? "")}</a>`
  }

  if (tag === "IMG") {
    const src = safeImageUrl(element.getAttribute("src") ?? "")
    if (!src) {
      return ""
    }

    const alt = escapeHtml(element.getAttribute("alt") ?? "")
    return `<img src="${escapeHtml(src)}" alt="${alt}" loading="lazy">`
  }

  if (tag === "B" || tag === "STRONG" || tag === "I" || tag === "EM" || tag === "U" || tag === "P" || tag === "DIV" || tag === "UL" || tag === "OL" || tag === "LI" || tag === "SPAN" || tag === "BLOCKQUOTE") {
    const lowerTag = tag.toLowerCase()
    return `<${lowerTag}>${children}</${lowerTag}>`
  }

  return children
}

export const sanitizeRichTextHtml = (value: string | null | undefined) => {
  const text = value?.trim() ?? ""
  if (!text) {
    return ""
  }

  if (!isHtmlLike(text)) {
    return renderMarkdownLikeText(text)
  }

  const parser = new DOMParser()
  const doc = parser.parseFromString(text, "text/html")
  return Array.from(doc.body.childNodes).map(sanitizeNode).join("").trim()
}

export const richTextHasContent = (value: string | null | undefined) => {
  const text = value?.trim() ?? ""
  if (!text) {
    return false
  }

  if (!isHtmlLike(text)) {
    return text.replace(/\s+/g, "").length > 0
  }

  const parser = new DOMParser()
  const doc = parser.parseFromString(text, "text/html")
  return (doc.body.textContent ?? "").replace(/\s+/g, "").length > 0
}

export const richTextToPlainText = (value: string | null | undefined) => {
  const text = value?.trim() ?? ""
  if (!text) {
    return ""
  }

  if (!isHtmlLike(text)) {
    return text
  }

  const parser = new DOMParser()
  const doc = parser.parseFromString(text, "text/html")
  return (doc.body.textContent ?? "").trim()
}
