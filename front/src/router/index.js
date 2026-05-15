import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    redirect: '/admin'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Login/Register.vue')
  },
  {
    path: '/phone-login',
    name: 'PhoneLogin',
    component: () => import('../views/Login/PhoneLogin.vue')
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('../views/User/User.vue')
  },
  {
    path: '/driver',
    name: 'Driver',
    component: () => import('../views/User/Driver.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin/Admin.vue'),
    children: [
      {
        path: '/admin/users',
        name: 'Users',
        component: () => import('../views/Admin/User.vue')
      },
      {
        path: '/admin/order',
        name: 'Order',
        component: () => import('../views/Admin/Order.vue')
      },
      {
        path: '/admin/pwd',
        name: 'Pwd',
        component: () => import('../views/Admin/Modifypwd.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router