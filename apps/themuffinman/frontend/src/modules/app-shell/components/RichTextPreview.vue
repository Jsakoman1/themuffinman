<script setup lang="ts">
import {computed} from "vue"

const props = defineProps<{content?: string | null}>()
const sanitized = computed(() => {
  const content = props.content ?? ""
  const template = document.createElement("template")
  template.innerHTML = content
  const allowed = new Set(["P", "BR", "STRONG", "B", "EM", "I", "U", "UL", "OL", "LI", "A"])
  template.content.querySelectorAll("*").forEach(node => {
    if (!allowed.has(node.tagName)) { node.replaceWith(...Array.from(node.childNodes)); return }
    Array.from(node.attributes).forEach(attribute => {
      if (node.tagName !== "A" || attribute.name !== "href" || !/^https?:\/\//i.test(attribute.value)) node.removeAttribute(attribute.name)
    })
    if (node.tagName === "A") { node.setAttribute("target", "_blank"); node.setAttribute("rel", "noreferrer") }
  })
  return template.innerHTML || (content ? `<p>${content.replace(/[&<>]/g, character => ({"&": "&amp;", "<": "&lt;", ">": "&gt;"}[character] ?? character))}</p>` : "<p></p>")
})
</script>

<template><div class="rich-text-preview" data-preview-model="sanitized-rich-text" data-content-surface="business-public-preview" aria-label="Formatted preview" v-html="sanitized" /></template>

<style scoped>
.rich-text-preview{line-height:1.6}.rich-text-preview :deep(p:first-child){margin-top:0}.rich-text-preview :deep(p:last-child){margin-bottom:0}.rich-text-preview :deep(ul),.rich-text-preview :deep(ol){padding-left:1.4rem}.rich-text-preview :deep(a){color:var(--success);text-decoration:underline}
</style>
