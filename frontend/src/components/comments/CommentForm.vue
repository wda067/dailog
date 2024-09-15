<template>
  <form>
    <div class="card-body p-0">
      <div v-if="!isLoggedIn">
        <div class="position-relative">
          <input
            :value="name"
            class="form-control form-control-name"
            placeholder="이름"
            type="text"
            @input="updateName"
          />
          <input
            :type="showPassword ? 'text' : 'password'"
            :value="curPassword"
            class="form-control form-control-password"
            placeholder="4자 이상의 비밀번호"
            @input="updatePassword"
          />
          <div class="icon-container" @click="togglePassword">
            <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
          </div>
        </div>
      </div>
      <textarea
        :value="content"
        class="form-control form-control-content mb-2"
        placeholder="여러분의 소중한 댓글을 입력해주세요."
        @input="updateContent"
      ></textarea>
      <div class="form-row justify-content-end">
        <slot name="actions"></slot>
      </div>
    </div>
  </form>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue';

const showPassword = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const props = defineProps({
  name: String,
  password: String,
  content: String,
  readonly: {
    type: Boolean,
    default: false,
  },
  isLoggedIn: Boolean,
});

const curPassword = computed(() => props.password);

const emits = defineEmits(['update:name', 'update:password', 'update:content']);

function updateName(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target) {
    emits('update:name', target.value);
  }
}

function updatePassword(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  if (target) {
    emits('update:password', target.value);
  }
}

function updateContent(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  if (target) {
    emits('update:content', target.value);
  }
}

watch(showPassword, () => {
});
</script>

<style scoped>
.form-row {
  display: flex;
}

.form-control-name {
  flex: 1;
  border-bottom-right-radius: 0;
  border-top-right-radius: 0;
  border-bottom-left-radius: 0;
  margin-right: 2px;
}

.form-control-password {
  flex: 1;
  border-bottom-left-radius: 0;
  border-top-left-radius: 0;
  border-bottom-right-radius: 0;
  margin-left: 2px;
}

.form-control-content {
  margin-top: 4px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.form-control:focus,
.form-control-password:focus,
.form-control-content:focus {
  border: 1px solid #007bff;
  box-shadow: none;
}

.position-relative {
  display: flex;
  align-items: center;
}

.icon-container {
  position: absolute;
  right: 15px;
  display: flex;
  align-items: center;
  height: 100%;
  cursor: pointer;
}
</style>
