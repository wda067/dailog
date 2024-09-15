<template>
  <TheHeader></TheHeader>
  <TheView></TheView>
  <AppAlert />
</template>

<script lang="ts" setup>
import TheHeader from '@/layouts/TheHeader.vue';
import TheView from '@/layouts/TheView.vue';
import AppAlert from '@/components/app/AppAlert.vue';
import { useHealthCheck } from '@/composables/useHealthCheck';
import { onMounted } from 'vue';

const { startServerMonitor } = useHealthCheck();

onMounted(() => {
  startServerMonitor();
});
</script>

<style scoped></style>
