<template>
  <main>
    <div class="container py-4">
      <RouterView></RouterView>
    </div>
  </main>
</template>

<script lang="ts" setup></script>

<style scoped>
.container {
  padding-left: 0; /* 기본 좌측 여백 설정 */
  padding-right: 0; /* 기본 우측 여백 설정 */
}

@media (min-width: 992px) {
  .container {
    padding-left: 4rem; /* 화면 너비가 992px 이상일 때 */
    padding-right: 4rem;
  }
}

@media (min-width: 1200px) {
  .container {
    padding-left: 5rem; /* 화면 너비가 1200px 이상일 때 */
    padding-right: 5rem;
  }
}
</style>
