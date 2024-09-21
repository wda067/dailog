<template>
  <table class="table">
    <thead>
    <tr>
      <th class="text-center col-id" scope="col">번호</th>
      <th class="text-center col-title" scope="col">제목</th>
      <th class="text-center col-author" scope="col">글쓴이</th>
      <th class="text-center col-date" scope="col">작성일</th>
      <!--        <th class="text-center col-date" scope="col">조회</th>-->
      <!--        <th class="text-center col-date" scope="col">추천</th>-->
    </tr>
    </thead>
    <tbody>
    <tr v-for="item in items" :key="item.id">
      <th class="text-center" scope="row">{{ item.id }}</th>
      <td class="text-left">
        <a
            class="post-title-link"
            href="#"
            @click.prevent="postClick(item.id)"
        >
          <span class="title-text">{{ truncateTitle(item.title) }}</span>
          <span v-if="item.commentCount > 0" class="comment-count"> [{{ item.commentCount }}]</span>
        </a>
      </td>
      <td class="text-center">{{ item.nickname }}</td>
      <td class="text-center">{{ formatDate(item.createdAt) }}</td>
    </tr>
    </tbody>
  </table>
</template>

<script lang="ts" setup>
import {format, isSameDay} from 'date-fns';

defineProps({
  items: {
    type: Array as () => Array<{
      id: string;
      title: string;
      createdAt: string;
      nickname: string;
      commentCount: number;
    }>,
    required: true,
  },
});

const emits = defineEmits(['postClick']);

const formatDate = (date: string) => {
  //date 매개변수 타입 명시
  const createdAt = new Date(date);
  const now = new Date();
  if (isSameDay(createdAt, now)) {
    return format(createdAt, 'HH:mm');
  } else {
    return format(createdAt, 'yyyy-MM-dd');
  }
};

const postClick = (id: string) => {
  emits('postClick', id);
};

const truncateTitle = (title: string) => {
  const maxLength = 25;
  return title.length > maxLength ? title.slice(0, maxLength) + '...' : title;
};
</script>

<style scoped>
.table {
  width: auto;
  table-layout: fixed;
}

th,
td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.col-id {
  width: 5%;
}

.col-title {
  width: 60%;
}

.col-author {
  width: 5%;
}

.col-date {
  width: 5%;
}

.post-title-link {
  color: #000000;
  text-decoration: none;
  transition: color 0.3s ease;
}

.post-title-link:hover {
  color: #1a1d20;
}

.post-title-link:hover .title-text {
  text-decoration: underline;
}

.comment-count {
  color: #FF0000;
  font-weight: bold;
  text-decoration: none;
}
</style>
