import {sanitizeRichTextHtml} from "./richText.ts"

const escapeHtml = (value: string) => {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#39;")
}

const formatInline = (value: string) => {
  let html = escapeHtml(value)
  const links: string[] = []

  html = html.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
  html = html.replace(/_(.+?)_/g, "<em>$1</em>")
  html = html.replace(/\[(.+?)\]\((https?:\/\/[^\s<)]+)\)/g, (_, label: string, href: string) => {
    const token = `@@LINK${links.length}@@`
    links.push(`<a href="${href}" target="_blank" rel="noreferrer noopener">${label}</a>`)
    return token
  })

  links.forEach((link, index) => {
    html = html.replaceAll(`@@LINK${index}@@`, link)
  })

  return html
}

export const renderProfileText = (value: string | null | undefined) => {
  const text = value?.trim()
  if (!text) {
    return ""
  }

  if (/<\/?[a-z][\s\S]*>/i.test(text)) {
    return sanitizeRichTextHtml(text)
  }

  const lines = text.split(/\r?\n/)
  const htmlParts: string[] = []
  const paragraphLines: string[] = []
  let inList = false

  const flushParagraph = () => {
    if (!paragraphLines.length) {
      return
    }

    htmlParts.push(`<p>${formatInline(paragraphLines.join(" "))}</p>`)
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

      htmlParts.push(`<li>${formatInline(listMatch[1])}</li>`)
      continue
    }

    closeList()
    paragraphLines.push(trimmed)
  }

  flushParagraph()
  closeList()

  return htmlParts.join("")
}

export const getProfileInitials = (username: string | null | undefined) => {
  const value = username?.trim() ?? ""
  if (!value) {
    return "U"
  }

  const parts = value.split(/\s+/).filter(Boolean)
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase()
  }

  return `${parts[0].charAt(0)}${parts[1].charAt(0)}`.toUpperCase()
}
