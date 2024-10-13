<template>
  <div>
    <AppGrid v-slot="{ item }" :items="members" col-class="col-3">
      <AppCard>
        <template #default>
          <div class="card-content">
            <strong>{{ (item as Member).nickname }}</strong>
            <small>{{ (item as Member).likedAt }}</small>
          </div>
        </template>
      </AppCard>
    </AppGrid>
    <nav aria-label="Page navigation example" class="mt-2">
      <ul class="pagination justify-content-center">
        <li :class="isPrevPage" class="page-item">
          <a
            aria-label="Previous"
            class="page-link"
            href="#"
            @click.prevent="goToPrevPage"
          >
          <span aria-hidden="true">
            <i class="bi bi-chevron-left"></i>
          </span>
          </a>
        </li>
        <li :class="isNextPage" class="page-item">
          <a
            aria-label="Next"
            class="page-link"
            href="#"
            @click.prevent="goToNextPage"
          >
          <span aria-hidden="true">
            <i class="bi bi-chevron-right"></i>
          </span>
          </a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script lang="ts" setup>

import { computed } from 'vue';
import AppCard from '@/components/app/AppCard.vue';
import AppGrid from '@/components/app/AppGrid.vue';

interface Member {
  nickname: string;
  likedAt: string;
}

const props = defineProps({
  members: {
    type: Array as () => Member[],
    required: true
  },
  currentPage: {
    type: Number,
    required: true,
  },
  pageCount: {
    type: Number,
    required: true,
  },
});

const emits = defineEmits(['page']);

const isPrevPage = computed(() => ({
  disabled: !(props.currentPage > 1),
}));
const isNextPage = computed(() => ({
  disabled: !(props.currentPage < props.pageCount),
}));

const goToPrevPage = () => {
  if (props.currentPage > 1) {
    emits('page', props.currentPage - 1);
  }
};

const goToNextPage = () => {
  if (props.currentPage < props.pageCount) {
    emits('page', props.currentPage + 1);
  }
};
</script>

<style scoped>
.card-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
</style>
