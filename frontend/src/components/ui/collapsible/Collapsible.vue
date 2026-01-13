<script setup lang="ts">
import { ref, provide, watch } from 'vue'

const props = defineProps<{
  defaultOpen?: boolean
  open?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const isOpen = ref(props.open !== undefined ? props.open : (props.defaultOpen || false))

watch(() => props.open, (newValue) => {
  if (newValue !== undefined) {
    isOpen.value = newValue
  }
})

watch(isOpen, (newValue) => {
  emit('update:open', newValue)
})

const toggle = () => {
  isOpen.value = !isOpen.value
}

provide('collapsible', {
  isOpen,
  toggle
})
</script>

<template>
  <div>
    <slot :isOpen="isOpen" :toggle="toggle" />
  </div>
</template>
