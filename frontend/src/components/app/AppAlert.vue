<template>
  <div class="app-alert">
    <TransitionGroup name="slide">
      <div
        v-for="item in alerts"
        :key="item.id"
        :class="typeStyle(item.type)"
        class="alert"
        role="alert"
      >
        {{ item.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script lang="ts" setup>
import { type Alert, useAlert } from '@/composables/useAlert';

const { alerts } = useAlert();
const typeStyle = (type: Alert['type']): string =>
  type === 'error' ? 'alert-danger' : 'alert-success';
</script>

<style scoped>
.app-alert {
  position: fixed;
  top: 67px;
  right: 10px;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.5s ease;
}

.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  transform: translateX(0px);
}
</style>
