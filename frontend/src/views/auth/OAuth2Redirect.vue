<template>
  <div></div>
</template>

<script lang="ts" setup>
import { onMounted } from 'vue';
import { useOAuth2Redirect } from '@/composables/useOAuth2Redirect';

onMounted(async () => {
  await useOAuth2Redirect();
});
</script>

<style scoped></style>
