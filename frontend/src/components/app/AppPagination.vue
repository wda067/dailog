<template>
  <nav aria-label="Page navigation example" class="mt-1">
    <ul class="pagination justify-content-center">
      <li :class="isPrevGroupPage" class="page-item">
        <a
          aria-label="Previous group"
          class="page-link"
          href="#"
          @click.prevent="goToPrevGroup"
        >
          <span aria-hidden="true">
            <i class="bi bi-chevron-double-left"></i>
          </span>
        </a>
      </li>
      <li :class="isPrevPage" class="page-item">
        <a
          aria-label="Previous"
          class="page-link"
          href="#"
          @click.prevent="$emit('page', currentPage - 1)"
        >
          <span aria-hidden="true">
            <i class="bi bi-chevron-left"></i>
          </span>
        </a>
      </li>
      <li
        v-for="page in visiblePages"
        :key="page"
        :class="{ active: currentPage === page }"
        class="page-item"
      >
        <a class="page-link" href="#" @click.prevent="$emit('page', page)">
          {{ page }}
        </a>
      </li>
      <li :class="isNextPage" class="page-item">
        <a
          aria-label="Next"
          class="page-link"
          href="#"
          @click.prevent="$emit('page', currentPage + 1)"
        >
          <span aria-hidden="true">
            <i class="bi bi-chevron-right"></i>
          </span>
        </a>
      </li>
      <li :class="isNextGroupPage" class="page-item">
        <a
          aria-label="Next group"
          class="page-link"
          href="#"
          @click.prevent="goToNextGroup"
        >
          <span aria-hidden="true">
            <i class="bi bi-chevron-double-right"></i>
          </span>
        </a>
      </li>
    </ul>
  </nav>
</template>

<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps({
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

const groupSize = 10;

const currentGroup = computed(() => {
  return Math.floor((props.currentPage - 1) / groupSize) + 1;
});

const visiblePages = computed(() => {
  const start = (currentGroup.value - 1) * groupSize + 1;
  const end = Math.min(start + groupSize - 1, props.pageCount);
  const pages = [];
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

const isPrevPage = computed(() => ({
  disabled: !(props.currentPage > 1),
}));
const isNextPage = computed(() => ({
  disabled: !(props.currentPage < props.pageCount),
}));
const isPrevGroupPage = computed(() => ({
  disabled: !(currentGroup.value > 1),
}));
const isNextGroupPage = computed(() => ({
  disabled: !(currentGroup.value < Math.ceil(props.pageCount / groupSize)),
}));

const goToPrevGroup = () => {
  const newPage = (currentGroup.value - 2) * groupSize + 1;
  if (newPage > 0) {
    emits('page', newPage);
  }
};

const goToNextGroup = () => {
  const newPage = currentGroup.value * groupSize + 1;
  if (newPage <= props.pageCount) {
    emits('page', newPage);
  }
};
</script>

<style scoped></style>
