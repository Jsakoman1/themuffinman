import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // Runtime branding is a frontend asset; reference source assets remain in docs.
  publicDir: "public",
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }

          if (id.includes('@tiptap') || id.includes('/prosemirror-') || id.includes('/markdown-it/')) {
            return 'editor'
          }

          if (id.includes('/vue/') || id.includes('/vue-router/')) {
            return 'vue-core'
          }

          return 'vendor'
        }
      }
    }
  }
})
