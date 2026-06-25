<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from "vue"
import {EditorContent, useEditor} from "@tiptap/vue-3"
import StarterKit from "@tiptap/starter-kit"
import Underline from "@tiptap/extension-underline"
import Link from "@tiptap/extension-link"
import Image from "@tiptap/extension-image"
import Placeholder from "@tiptap/extension-placeholder"
import {compressImageFile} from "../../shared/imageCompression.ts"
import {renderProfileText} from "../../shared/profileFormatting.ts"
import {richTextHasContent, sanitizeRichTextHtml} from "../../shared/richText.ts"

const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
  toolbarLabel?: string
}>(), {
  modelValue: "",
  placeholder: "Write something...",
  toolbarLabel: "Text tools",
})

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void
}>()

type ToolbarAction = {
  key: string
  label: string
  title: string
  active: () => boolean
  onClick: () => void
  ghost?: boolean
}

const imageInputRef = ref<HTMLInputElement | null>(null)
const isFocused = ref(false)
const isMoreOpen = ref(false)
const isSyncingFromModel = ref(false)

const normalizeIncomingContent = (value: string) => {
  const text = value.trim()
  if (!text) {
    return ""
  }

  return /<\/?[a-z][\s\S]*>/i.test(text)
    ? sanitizeRichTextHtml(text)
    : renderProfileText(text)
}

const normalizeOutgoingContent = (value: string) => {
  const sanitized = sanitizeRichTextHtml(value)
  return richTextHasContent(sanitized) ? sanitized : ""
}

const editor = useEditor({
  content: normalizeIncomingContent(props.modelValue),
  extensions: [
    StarterKit.configure({
      heading: false,
      codeBlock: false,
      code: false,
      horizontalRule: false,
    }),
    Underline,
    Link.configure({
      openOnClick: false,
      autolink: true,
      defaultProtocol: "https",
      HTMLAttributes: {
        target: "_blank",
        rel: "noreferrer noopener",
      },
    }),
    Image.configure({
      inline: false,
      allowBase64: true,
      HTMLAttributes: {
        loading: "lazy",
      },
    }),
    Placeholder.configure({
      placeholder: props.placeholder,
    }),
  ],
  editorProps: {
    attributes: {
      class: "rich-text-editor__surface",
      spellcheck: "true",
    },
  },
  onFocus: () => {
    isFocused.value = true
  },
  onBlur: () => {
    isFocused.value = false
    isMoreOpen.value = false
  },
  onSelectionUpdate: () => {
    isMoreOpen.value = false
  },
  onUpdate: ({editor: currentEditor}) => {
    if (isSyncingFromModel.value) {
      return
    }

    emit("update:modelValue", normalizeOutgoingContent(currentEditor.getHTML()))
  },
})

const isEmpty = computed(() => editor.value?.isEmpty ?? !richTextHasContent(props.modelValue))

const syncEditorFromModel = (value: string) => {
  if (!editor.value) {
    return
  }

  const normalized = normalizeIncomingContent(value)
  const current = normalizeOutgoingContent(editor.value.getHTML())
  const incoming = normalizeOutgoingContent(normalized)
  if (current === incoming) {
    return
  }

  isSyncingFromModel.value = true
  editor.value.commands.setContent(normalized, {emitUpdate: false})
  isSyncingFromModel.value = false
}

watch(() => props.modelValue, (value) => {
  syncEditorFromModel(value)
})

const toggleInline = (mark: "bold" | "italic" | "underline") => {
  if (!editor.value) {
    return
  }

  const chain = editor.value.chain().focus()
  if (mark === "bold") {
    chain.toggleBold().run()
    return
  }

  if (mark === "italic") {
    chain.toggleItalic().run()
    return
  }

  chain.toggleUnderline().run()
}

const toggleBulletList = () => {
  editor.value?.chain().focus().toggleBulletList().run()
}

const toggleOrderedList = () => {
  editor.value?.chain().focus().toggleOrderedList().run()
}

const toggleQuote = () => {
  editor.value?.chain().focus().toggleBlockquote().run()
}

const toggleLink = () => {
  if (!editor.value) {
    return
  }

  if (editor.value.isActive("link")) {
    editor.value.chain().focus().unsetLink().run()
    isMoreOpen.value = false
    return
  }

  const href = window.prompt("Link URL", "https://")
  if (!href?.trim()) {
    return
  }

  editor.value.chain().focus().extendMarkRange("link").setLink({href: href.trim()}).run()
  isMoreOpen.value = false
}

const clearFormatting = () => {
  editor.value?.chain().focus().unsetAllMarks().clearNodes().run()
  isMoreOpen.value = false
}

const insertImage = async (file: File | null) => {
  if (!file || !editor.value) {
    return
  }

  try {
    const imageDataUrl = await compressImageFile(file, 1400, 0.86)
    editor.value.chain().focus().setImage({
      src: imageDataUrl,
      alt: file.name,
    }).run()
    isMoreOpen.value = false
  } catch {
    // Keep the editor untouched on image failures.
  }
}

const inlineActions = computed<ToolbarAction[]>(() => [
  {
    key: "bold",
    label: "B",
    title: "Bold",
    active: () => !!editor.value?.isActive("bold"),
    onClick: () => toggleInline("bold"),
  },
  {
    key: "italic",
    label: "I",
    title: "Italic",
    active: () => !!editor.value?.isActive("italic"),
    onClick: () => toggleInline("italic"),
  },
  {
    key: "underline",
    label: "U",
    title: "Underline",
    active: () => !!editor.value?.isActive("underline"),
    onClick: () => toggleInline("underline"),
  },
])

const moreActions = computed<ToolbarAction[]>(() => [
  {
    key: "unordered-list",
    label: "List",
    title: "Bullet list",
    active: () => !!editor.value?.isActive("bulletList"),
    onClick: toggleBulletList,
  },
  {
    key: "ordered-list",
    label: "Numbered",
    title: "Numbered list",
    active: () => !!editor.value?.isActive("orderedList"),
    onClick: toggleOrderedList,
  },
  {
    key: "quote",
    label: "Quote",
    title: "Quote",
    active: () => !!editor.value?.isActive("blockquote"),
    onClick: toggleQuote,
  },
  {
    key: "link",
    label: editor.value?.isActive("link") ? "Unlink" : "Link",
    title: editor.value?.isActive("link") ? "Remove link" : "Add link",
    active: () => !!editor.value?.isActive("link"),
    onClick: toggleLink,
  },
  {
    key: "image",
    label: "Image",
    title: "Insert image",
    active: () => false,
    onClick: () => imageInputRef.value?.click(),
  },
  {
    key: "clear",
    label: "Clear",
    title: "Clear formatting",
    active: () => false,
    onClick: clearFormatting,
    ghost: true,
  },
])

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<template>
  <div class="rich-text-editor" :class="{ 'rich-text-editor--focused': isFocused }">
    <div class="rich-text-editor__frame">
      <div class="rich-text-editor__toolbar" :aria-label="toolbarLabel">
        <div class="rich-text-editor__group">
          <button
            v-for="action in inlineActions"
            :key="action.key"
            class="rich-text-editor__button"
            :class="{ 'rich-text-editor__button--active': action.active() }"
            type="button"
            :title="action.title"
            :aria-label="action.title"
            @mousedown.prevent
            @click="action.onClick()"
          >
            <span
              class="rich-text-editor__button-glyph"
              :class="{
                'rich-text-editor__button-glyph--italic': action.key === 'italic',
                'rich-text-editor__button-glyph--underline': action.key === 'underline'
              }"
            >
              {{ action.label }}
            </span>
          </button>
        </div>

        <div class="rich-text-editor__menu">
          <button
            class="rich-text-editor__button rich-text-editor__button--wide"
            type="button"
            :class="{ 'rich-text-editor__button--active': isMoreOpen }"
            title="More tools"
            @mousedown.prevent
            @click="isMoreOpen = !isMoreOpen"
          >
            More
          </button>

          <div v-if="isMoreOpen" class="rich-text-editor__menu-panel">
            <button
              v-for="action in moreActions"
              :key="action.key"
              class="rich-text-editor__menu-item"
              :class="{
                'rich-text-editor__menu-item--active': action.active(),
                'rich-text-editor__menu-item--ghost': action.ghost
              }"
              type="button"
              :title="action.title"
              @mousedown.prevent
              @click="action.onClick()"
            >
              {{ action.label }}
            </button>
          </div>
        </div>

        <div class="rich-text-editor__toolbar-spacer" />
      </div>

      <div class="rich-text-editor__canvas">
        <EditorContent
          v-if="editor"
          :editor="editor"
          class="rich-text-editor__surface-shell"
          :class="{ 'rich-text-editor__surface-shell--empty': isEmpty }"
        />
      </div>
    </div>

    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      class="visually-hidden"
      @change="insertImage(($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
    >
  </div>
</template>
