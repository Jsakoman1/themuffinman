<template>
  <footer class="app-form-footer"><slot name="secondary" /><slot name="primary" /></footer>
</template>

<style scoped>
.app-form-footer{display:flex;justify-content:flex-end;align-items:center;gap:.45rem;flex-wrap:wrap;margin-top:.25rem}.app-form-footer :deep(a),.app-form-footer :deep(button){display:inline-flex;align-items:center;justify-content:center;min-height:2.3rem;border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .8rem;font:inherit;font-size:.82rem;font-weight:650;background:transparent;cursor:pointer}.app-form-footer :deep(button[type="submit"]){background:#17221a;color:#f8f8f4}.app-form-footer :deep(button:disabled){cursor:not-allowed;opacity:.55}
</style>
