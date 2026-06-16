<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from '@/components/AppShell.vue'

const route = useRoute()
const useBlankLayout = computed(() => route.meta.layout === 'blank')
</script>

<template>
  <router-view v-if="useBlankLayout" />
  <AppShell v-else />
</template>
