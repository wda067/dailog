<template>
  <div>
    <form>
      <div class="mb-3">
        <input
          :value="title"
          class="form-control"
          placeholder="제목을 입력해 주세요."
          type="text"
          @input="updateTitle"
        />
      </div>
      <div class="mb-3">
        <textarea
          :value="content"
          class="form-control"
          placeholder="내용을 입력해 주세요."
          rows="5"
          @input="updateContent"
        ></textarea>
      </div>
      <div class="d-flex gap-2 mt-4">
        <slot name="actions"></slot>
      </div>
    </form>
  </div>
</template>

<script lang="ts" setup>
defineProps({
  title: String,
  content: String,
});
const emits = defineEmits(['update:title', 'update:content']);

function updateTitle(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target) {
    emits('update:title', target.value);
  }
}

function updateContent(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  if (target) {
    emits('update:content', target.value);
  }
}
</script>

<style scoped></style>
