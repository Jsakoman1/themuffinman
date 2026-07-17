<script setup lang="ts">
import {onBeforeUnmount} from "vue"
import {EditorContent, useEditor} from "@tiptap/vue-3"
import StarterKit from "@tiptap/starter-kit"
import Underline from "@tiptap/extension-underline"
import Link from "@tiptap/extension-link"
import Placeholder from "@tiptap/extension-placeholder"

const props = withDefaults(defineProps<{modelValue: string; label?: string; placeholder?: string}>(), {label: "Rich text", placeholder: "Write something useful…"})
const emit = defineEmits<{"update:modelValue": [value: string]}>()
const editor = useEditor({
  content: props.modelValue,
  extensions: [StarterKit, Underline, Link.configure({openOnClick: false}), Placeholder.configure({placeholder: props.placeholder})],
  onUpdate: ({editor: value}) => emit("update:modelValue", value.getHTML())
})
onBeforeUnmount(() => editor.value?.destroy())
</script>

<template>
  <div class="rich-text-editor" :aria-label="label">
    <div v-if="editor" class="rich-text-editor__toolbar" role="toolbar" :aria-label="`${label} formatting`">
      <button type="button" :class="{active: editor.isActive('bold')}" title="Bold" aria-label="Bold" @click="editor.chain().focus().toggleBold().run()"><strong>B</strong></button>
      <button type="button" :class="{active: editor.isActive('italic')}" title="Italic" aria-label="Italic" @click="editor.chain().focus().toggleItalic().run()"><em>I</em></button>
      <button type="button" :class="{active: editor.isActive('underline')}" title="Underline" aria-label="Underline" @click="editor.chain().focus().toggleUnderline().run()"><u>U</u></button>
      <button type="button" :class="{active: editor.isActive('bulletList')}" title="Bullet list" aria-label="Bullet list" @click="editor.chain().focus().toggleBulletList().run()">• List</button>
      <button type="button" :class="{active: editor.isActive('orderedList')}" title="Numbered list" aria-label="Numbered list" @click="editor.chain().focus().toggleOrderedList().run()">1. List</button>
      <button type="button" title="Clear formatting" aria-label="Clear formatting" @click="editor.chain().focus().clearNodes().unsetAllMarks().run()">Clear</button>
    </div>
    <EditorContent :editor="editor" class="rich-text-editor__content" />
  </div>
</template>

<style scoped>
.rich-text-editor{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface)}.rich-text-editor__toolbar{display:flex;flex-wrap:wrap;gap:.2rem;padding:.4rem;border-bottom:1px solid var(--border-subtle);background:var(--surface-muted)}.rich-text-editor__toolbar button{border:0;border-radius:.45rem;padding:.35rem .5rem;background:transparent;color:var(--text-muted);font:inherit;font-size:.78rem;cursor:pointer}.rich-text-editor__toolbar button:hover,.rich-text-editor__toolbar button.active{background:var(--surface);color:var(--text)}.rich-text-editor__content{min-height:9rem;padding:.75rem}.rich-text-editor__content :deep(.tiptap){min-height:7.5rem;outline:none}.rich-text-editor__content :deep(.tiptap p.is-editor-empty:first-child::before){float:left;height:0;color:var(--text-soft);content:attr(data-placeholder);pointer-events:none}.rich-text-editor__content :deep(.tiptap ul),.rich-text-editor__content :deep(.tiptap ol){padding-left:1.4rem}
</style>
