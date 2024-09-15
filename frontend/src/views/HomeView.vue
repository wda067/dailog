<template>
  <div>
    <h2>Home View</h2>
    <hr class="my-4" />
    <AppGrid v-slot="{ item }" :items="items" col-class="col-4">
      <AppCard>{{ item }}</AppCard>
    </AppGrid>
  </div>
</template>

<script lang="ts" setup>
import AppCard from '@/components/app/AppCard.vue';
import { ref } from 'vue';
import AppGrid from '@/components/app/AppGrid.vue';

const items = ref(['a', 'b', 'c', 'd']);
</script>

<style scoped></style>
