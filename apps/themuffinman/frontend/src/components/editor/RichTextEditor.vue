<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {renderProfileText} from "../../shared/profileFormatting.ts"
import {compressImageFile} from "../../shared/imageCompression.ts"
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

type CommandState = {
  bold: boolean
  italic: boolean
  underline: boolean
  unorderedList: boolean
  orderedList: boolean
  quote: boolean
  hasLink: boolean
}

const editorRef = ref<HTMLElement | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)
const isFocused = ref(false)
const savedRange = ref<Range | null>(null)
const commandState = ref<CommandState>({
  bold: false,
  italic: false,
  underline: false,
  unorderedList: false,
  orderedList: false,
  quote: false,
  hasLink: false
})

const isEmpty = computed(() => !richTextHasContent(props.modelValue))

const syncEditor = () => {
  if (!editorRef.value) {
    return
  }

  const html = props.modelValue.trim() ? renderProfileText(props.modelValue) : ""
  if (editorRef.value.innerHTML !== html) {
    editorRef.value.innerHTML = html
  }
}

const saveSelection = () => {
  const selection = window.getSelection()
  const editor = editorRef.value
  if (!selection || !editor || selection.rangeCount === 0) {
    return
  }

  const range = selection.getRangeAt(0)
  if (!editor.contains(range.commonAncestorContainer)) {
    return
  }

  savedRange.value = range.cloneRange()
}

const restoreSelection = () => {
  const editor = editorRef.value
  const range = savedRange.value
  if (!editor || !range) {
    return false
  }

  const selection = window.getSelection()
  if (!selection) {
    return false
  }

  selection.removeAllRanges()
  selection.addRange(range)
  editor.focus()
  return true
}

const queryCommandState = (command: string) => {
  try {
    return document.queryCommandState(command)
  } catch {
    return false
  }
}

const findParentTag = (node: Node | null, tags: string[]) => {
  const editor = editorRef.value
  let current = node

  while (current && current !== editor) {
    if (current instanceof HTMLElement && tags.includes(current.tagName)) {
      return current
    }
    current = current.parentNode
  }

  return null
}

const refreshCommandState = () => {
  const selection = window.getSelection()
  const anchorNode = selection?.anchorNode ?? null

  commandState.value = {
    bold: queryCommandState("bold"),
    italic: queryCommandState("italic"),
    underline: queryCommandState("underline"),
    unorderedList: queryCommandState("insertUnorderedList"),
    orderedList: queryCommandState("insertOrderedList"),
    quote: !!findParentTag(anchorNode, ["BLOCKQUOTE"]),
    hasLink: !!findParentTag(anchorNode, ["A"])
  }
}

const emitValue = () => {
  if (!editorRef.value) {
    return
  }

  const html = sanitizeRichTextHtml(editorRef.value.innerHTML)
  if (richTextHasContent(html)) {
    emit("update:modelValue", html)
    refreshCommandState()
    return
  }

  editorRef.value.innerHTML = ""
  emit("update:modelValue", "")
  refreshCommandState()
}

const execute = (command: string, value?: string) => {
  restoreSelection()
  document.execCommand(command, false, value)
  emitValue()
  saveSelection()
}

const toggleQuote = () => {
  restoreSelection()

  if (commandState.value.quote) {
    document.execCommand("formatBlock", false, "p")
  } else {
    document.execCommand("formatBlock", false, "blockquote")
  }

  emitValue()
  saveSelection()
}

const clearFormatting = () => {
  restoreSelection()
  document.execCommand("removeFormat")
  document.execCommand("unlink")
  emitValue()
  saveSelection()
}

const toggleLink = () => {
  if (commandState.value.hasLink) {
    restoreSelection()
    document.execCommand("unlink")
    emitValue()
    saveSelection()
    return
  }

  const href = window.prompt("Link URL", "https://")
  if (!href) {
    return
  }

  restoreSelection()
  document.execCommand("createLink", false, href.trim())
  emitValue()
  saveSelection()
}

const insertImage = async (file: File | null) => {
  if (!file) {
    return
  }

  try {
    const imageDataUrl = await compressImageFile(file, 1400, 0.86)
    restoreSelection()
    document.execCommand(
      "insertHTML",
      false,
      `<img src="${imageDataUrl}" alt="${file.name.replaceAll("\"", "&quot;")}" loading="lazy">`
    )
    emitValue()
    saveSelection()
  } catch {
    // Keep the editor untouched on image failures.
  }
}

const focusEditor = () => {
  editorRef.value?.focus()
}

const handleInput = () => {
  emitValue()
}

const handlePaste = (event: ClipboardEvent) => {
  event.preventDefault()
  const text = event.clipboardData?.getData("text/plain") ?? ""
  document.execCommand("insertText", false, text)
  emitValue()
}

const handleToolbarMouseDown = () => {
  saveSelection()
}

const handleSelectionChange = () => {
  if (!isFocused.value) {
    return
  }

  saveSelection()
  refreshCommandState()
}

watch(() => props.modelValue, () => {
  if (isFocused.value) {
    return
  }

  void nextTick(() => {
    syncEditor()
    refreshCommandState()
  })
}, {immediate: true})

onMounted(() => {
  syncEditor()
  refreshCommandState()
  document.addEventListener("selectionchange", handleSelectionChange)
})

onBeforeUnmount(() => {
  document.removeEventListener("selectionchange", handleSelectionChange)
})
</script>

<template>
  <div class="rich-text-editor" :class="{ 'rich-text-editor--focused': isFocused }">
    <div class="rich-text-editor__frame">
      <div class="rich-text-editor__toolbar" :aria-label="toolbarLabel">
        <div class="rich-text-editor__group">
          <button
            class="rich-text-editor__button"
            :class="{ 'rich-text-editor__button--active': commandState.bold }"
            type="button"
            title="Bold"
            aria-label="Bold"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="execute('bold')"
          >
            <span class="rich-text-editor__button-glyph"><strong>B</strong></span>
          </button>
          <button
            class="rich-text-editor__button"
            :class="{ 'rich-text-editor__button--active': commandState.italic }"
            type="button"
            title="Italic"
            aria-label="Italic"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="execute('italic')"
          >
            <span class="rich-text-editor__button-glyph rich-text-editor__button-glyph--italic">I</span>
          </button>
          <button
            class="rich-text-editor__button"
            :class="{ 'rich-text-editor__button--active': commandState.underline }"
            type="button"
            title="Underline"
            aria-label="Underline"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="execute('underline')"
          >
            <span class="rich-text-editor__button-glyph rich-text-editor__button-glyph--underline">U</span>
          </button>
        </div>

        <div class="rich-text-editor__group">
          <button
            class="rich-text-editor__button rich-text-editor__button--wide"
            :class="{ 'rich-text-editor__button--active': commandState.unorderedList }"
            type="button"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="execute('insertUnorderedList')"
          >
            List
          </button>
          <button
            class="rich-text-editor__button rich-text-editor__button--wide"
            :class="{ 'rich-text-editor__button--active': commandState.orderedList }"
            type="button"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="execute('insertOrderedList')"
          >
            Steps
          </button>
          <button
            class="rich-text-editor__button rich-text-editor__button--wide"
            :class="{ 'rich-text-editor__button--active': commandState.quote }"
            type="button"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="toggleQuote"
          >
            Quote
          </button>
        </div>

        <div class="rich-text-editor__group rich-text-editor__group--grow">
          <button
            class="rich-text-editor__button rich-text-editor__button--wide"
            :class="{ 'rich-text-editor__button--active': commandState.hasLink }"
            type="button"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="toggleLink"
          >
            {{ commandState.hasLink ? "Unlink" : "Link" }}
          </button>
          <button
            class="rich-text-editor__button rich-text-editor__button--wide"
            type="button"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="imageInputRef?.click()"
          >
            Image
          </button>
          <button
            class="rich-text-editor__button rich-text-editor__button--ghost"
            type="button"
            @mousedown.prevent="handleToolbarMouseDown"
            @click="clearFormatting"
          >
            Clear
          </button>
        </div>
      </div>

      <div class="rich-text-editor__canvas" @click="focusEditor">
        <div
          ref="editorRef"
          class="rich-text-editor__surface"
          :class="{ 'rich-text-editor__surface--empty': isEmpty }"
          contenteditable="true"
          spellcheck="true"
          role="textbox"
          :data-placeholder="placeholder"
          @focus="isFocused = true"
          @blur="isFocused = false; refreshCommandState()"
          @mouseup="saveSelection"
          @keyup="saveSelection"
          @input="handleInput"
          @paste="handlePaste"
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
