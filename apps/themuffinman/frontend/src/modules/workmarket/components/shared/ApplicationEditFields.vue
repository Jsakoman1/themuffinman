<script setup lang="ts">
import {computed, ref} from "vue"
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import InlineEditableField from "./InlineEditableField.vue"
import {formatApplicationPrice} from "../../shared/pricing.ts"

const props = defineProps<{
  message: string
  price: string
  pricePlaceholder?: string
  quickfillLabel?: string
  inlineEditable?: boolean
  showPrice?: boolean
}>()

const emit = defineEmits<{
  (event: "update:message", value: string): void
  (event: "update:price", value: string): void
  (event: "quickfill"): void
}>()

const editingMessage = ref(false)
const editingPrice = ref(false)
const displayPrice = computed(() => props.price?.trim() ? `$ ${props.price.trim()}` : formatApplicationPrice(null))
</script>

<template>
  <div class="ui-edit-form ui-edit-form--dialog ui-edit-form--application">
    <InlineEditableField
      v-if="props.inlineEditable !== false"
      label="Message"
      :editing="editingMessage"
      field-class="ui-edit-field--message"
      @toggle="editingMessage = !editingMessage"
    >
      <template #editor>
        <RichTextEditor
          :model-value="message"
          placeholder=""
          toolbar-label="Message tools"
          @update:model-value="emit('update:message', $event)"
        />
      </template>
      <template #display>
        <ProfileBio
          class="ui-content-prose ui-content-prose--flat"
          :text="message"
          placeholder="No message yet."
        />
      </template>
    </InlineEditableField>

    <div v-else class="ui-edit-field ui-edit-field--message field">
      <span class="label">Message</span>
      <RichTextEditor
        :model-value="message"
        placeholder=""
        toolbar-label="Message tools"
        @update:model-value="emit('update:message', $event)"
      />
    </div>

    <InlineEditableField
      v-if="props.inlineEditable !== false && props.showPrice !== false"
      label="Proposed price"
      :editing="editingPrice"
      field-class="ui-edit-field--price"
      @toggle="editingPrice = !editingPrice"
    >
      <template #editor>
        <div class="ui-inline-readonly-stack">
          <button v-if="quickfillLabel" class="button button--ghost calendar-application-form__quickfill" type="button" @click="emit('quickfill')">
            {{ quickfillLabel }}
          </button>
          <div class="ui-amount-input">
            <span class="ui-amount-input__symbol" aria-hidden="true">$</span>
            <input
              :value="price"
              class="input ui-amount-input__input"
              inputmode="decimal"
              :placeholder="pricePlaceholder"
              @input="emit('update:price', ($event.target as HTMLInputElement).value)"
            />
          </div>
        </div>
      </template>
      <template #display>
        <div class="ui-inline-readonly-text">{{ displayPrice }}</div>
      </template>
    </InlineEditableField>

    <div v-else-if="props.showPrice !== false" class="ui-edit-field ui-edit-field--price field">
      <div class="field__header">
        <span class="label">Proposed price</span>
        <button v-if="quickfillLabel" class="button button--ghost calendar-application-form__quickfill" type="button" @click="emit('quickfill')">
          {{ quickfillLabel }}
        </button>
      </div>
      <div class="ui-amount-input">
        <span class="ui-amount-input__symbol" aria-hidden="true">$</span>
        <input
          :value="price"
          class="input ui-amount-input__input"
          inputmode="decimal"
          :placeholder="pricePlaceholder"
          @input="emit('update:price', ($event.target as HTMLInputElement).value)"
        />
      </div>
    </div>
  </div>
</template>
