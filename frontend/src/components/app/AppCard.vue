<template>
  <div class="card">
    <div v-if="$slots.header" class="card-hearer">
      <slot name="header"></slot>
    </div>
    <div v-if="$slots.default" class="card-body">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<style scoped></style>
