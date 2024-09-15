<template>
  <div>
    <h2>회원 목록</h2>
    <hr class="my-4" />

    <table class="table">
      <thead>
        <tr>
          <th class="text-center col-id" scope="col">번호</th>
          <th class="text-center col-email" scope="col">이메일</th>
          <th class="text-center col-name" scope="col">이름</th>
          <th class="text-center col-date" scope="col">가입일</th>
          <th class="text-center col-role" scope="col">등급</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="member in members" :key="member.id">
          <th class="text-center" scope="row">{{ member.id }}</th>
          <td class="text-center">
            <a
              class="member-link"
              href="#"
              @click.prevent="goToDetail(member.id)"
            >
              {{ member.email }}</a
            >
          </td>
          <td class="text-center">{{ member.name }}</td>
          <td class="text-center">{{ member.createdAt }}</td>
          <td class="text-center">{{ member.role }}</td>
        </tr>
      </tbody>
    </table>

    <AppPagination
      :current-page="params.page"
      :page-count="pageCount"
      @page="page => (params.page = page)"
    />
    <AppFilter :searchParams="params" :type="'member'" @search="updateParams" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import AppPagination from '@/components/app/AppPagination.vue';
import AppFilter from '@/components/app/AppFilter.vue';
import { useRouter } from 'vue-router';
import axiosInstance from '@/composables/useApi';

const router = useRouter();

const members = ref(
  [] as Array<{
    id: string;
    email: string;
    name: string;
    createdAt: string;
    role: string;
  }>,
);

const params = ref({
  page: 1,
  size: 10,
  searchDateType: '',
  searchType: 'email',
  searchQuery: '',
});

const totalCount = ref(0);
const pageCount = computed(() =>
  Math.ceil(totalCount.value / params.value.size),
);

const fetchMembers = async () => {
  try {
    const { data } = await axiosInstance.get('/api/admin/members', {
      params: params.value,
    });
    members.value = data.items;
    totalCount.value = data.totalCount;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.log('error:{}', Error);
    }
  }
};

const updateParams = () => {
  params.value.page = 1;
  fetchMembers();
};

watch(
  () => params.value.page,
  () => {
    fetchMembers();
  },
);

const goToDetail = (id: string) => {
  router.push({ name: 'AdminMemberDetail', params: { id } });
};

onMounted(fetchMembers);
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

.col-email {
  width: 15%;
}

.col-name {
  width: 10%;
}

.col-date {
  width: 15%;
}

.col-role {
  width: 5%;
}

.member-link {
  color: #000000;
  text-decoration: none;
  transition: color 0.3s ease;
}

.member-link:hover,
.member-link:focus {
  color: #1a1d20;
  text-decoration: underline;
}
</style>
