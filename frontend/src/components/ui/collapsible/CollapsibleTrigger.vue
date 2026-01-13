<script setup lang="ts">
import { inject } from 'vue'

const collapsible = inject<{
  isOpen: { value: boolean }
  toggle: () => void
}>('collapsible')

const handleClick = (e: Event) => {
  e.preventDefault()
  e.stopPropagation()
  collapsible?.toggle()
}
</script>

<template>
  <div @click="handleClick" class="cursor-pointer w-full">
    <slot :isOpen="collapsible?.isOpen.value" />
  </div>
</template>
