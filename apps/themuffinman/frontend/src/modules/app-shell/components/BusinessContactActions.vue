<script setup lang="ts">
defineProps<{ email?: string; phone?: string; whatsapp?: string; website?: string; address?: string }>()
</script>

<template>
  <section v-if="email || phone || whatsapp || website || address" class="contact-actions" aria-label="Contact and location">
    <p v-if="address">{{ address }}</p>
    <div>
      <a v-if="phone" :href="`tel:${phone}`">Call</a>
      <a v-if="email" :href="`mailto:${email}`">Email</a>
      <a v-if="whatsapp" :href="`https://wa.me/${whatsapp.replace(/\D/g, '')}`" target="_blank" rel="noopener">WhatsApp</a>
      <a v-if="website" :href="website" target="_blank" rel="noopener">Website</a>
    </div>
  </section>
</template>

<style scoped>
.contact-actions { display: grid; gap: var(--space-2); color: var(--text-muted); }
.contact-actions p { margin: 0; }
.contact-actions div { display: flex; flex-wrap: wrap; gap: var(--space-2); }
a { border: 1px solid var(--border-subtle); border-radius: var(--radius-control); color: var(--text); padding: var(--space-1) var(--space-2); text-decoration: none; }
</style>
