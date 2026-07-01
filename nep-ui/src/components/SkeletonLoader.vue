<template>
  <div class="skeleton-wrapper" :style="{ width, height }">
    <div class="skeleton" :style="{ width: '100%', height: '100%' }"></div>
  </div>
</template>

<script setup>
defineProps({
  width: { type: String, default: '100%' },
  height: { type: String, default: '20px' },
})
</script>

<style scoped>
.skeleton-wrapper {
  border-radius: var(--radius-sm);
  overflow: hidden;
}
</style>
