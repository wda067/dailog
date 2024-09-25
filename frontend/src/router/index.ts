import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import PostWriteView from '@/views/posts/PostWriteView.vue';
import PostDetailView from '@/views/posts/PostDetailView.vue';
import PostEditView from '@/views/posts/PostEditView.vue';
import PostListView from '@/views/posts/PostListView.vue';
import LoginView from '@/views/auth/LoginView.vue';
import JoinView from '@/views/auth/JoinView.vue';
import { useAlert } from '@/composables/useAlert';
import AdminMemberListView from '@/views/admins/AdminMemberListView.vue';
import MemberDetailView from '@/views/members/MemberDetailView.vue';
import AdminMemberDetailView from '@/views/admins/AdminMemberDetailView.vue';
import OAuth2Redirect from '@/views/auth/OAuth2Redirect.vue';
import { useAuthStore } from '@/stores/auth';
import NotFoundView from '@/views/errors/NotFoundView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'HomeView',
      component: PostListView,
    },
    {
      path: '/login',
      name: 'LoginView',
      component: LoginView,
    },
    {
      path: '/join',
      name: 'JoinView',
      component: JoinView,
    },
    {
      path: '/posts',
      name: 'PostList',
      component: PostListView,
    },
    {
      path: '/posts/write',
      name: 'PostWrite',
      component: PostWriteView,
      meta: { requiresAuth: true },
    },
    {
      path: '/posts/:id',
      name: 'PostDetail',
      component: PostDetailView,
      props: true,
    },
    {
      path: '/posts/:id/edit',
      name: 'PostEdit',
      component: PostEditView,
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/members',
      name: 'AdminMemberList',
      component: AdminMemberListView,
      meta: { requiresAuth: true },
    },
    {
      path: '/members/:id',
      name: 'MemberDetail',
      component: MemberDetailView,
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/members/:id',
      name: 'AdminMemberDetail',
      component: AdminMemberDetailView,
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/oauth2-jwt-header',
      name: 'OAuth2Redirect',
      component: OAuth2Redirect,
    },
    {
      path: '/:catchAll(.*)',
      name: 'NotFound',
      component: NotFoundView,
      props: true,
    },
  ],
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const { vAlert } = useAlert();

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    vAlert('로그인 해주세요.');
    next({ name: 'LoginView' });
  } else {
    next();
  }
});

export default router;
