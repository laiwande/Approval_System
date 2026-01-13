<script setup lang="ts">
import { inject } from 'vue'

const collapsible = inject<{
  isOpen: { value: boolean }
}>('collapsible')
</script>

<template>
  <div v-if="collapsible?.isOpen.value" class="overflow-hidden transition-all animate-in slide-in-from-top-1">
    <slot />
  </div>
</template>
