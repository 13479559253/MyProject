<template>
  <el-container class="admin-container">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="aside">
      <div class="admin-logo">
        <div class="logo-icon">CC</div>
        <span class="logo-text">CC打车管理后台</span>
      </div>

      <el-menu
        default-active="1"
        class="menu"
        background-color="#ffffff"
        text-color="#666"
        active-text-color="#32d699"
        router
      >
        <el-menu-item index="1">
          <el-icon><HomeFilled /></el-icon>
          <span>首页概览</span>
        </el-menu-item>

        <el-sub-menu index="2">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户表</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="3">
          <el-icon><Operation /></el-icon>
          <span>司机申请</span>
        </el-menu-item>
        <el-sub-menu index="4">
          <template #title>
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/admin/order">订单查询</el-menu-item>
          <el-menu-item index="">被投诉订单判定</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/pwd">
          <el-icon><Setting /></el-icon>
          <span>修改密码</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">欢迎进入管理后台</div>
        <div class="header-right">
          <el-avatar size="36" background="#32d699">管</el-avatar>
          <span class="admin-name">{{ name }}</span>
          <el-button type="text" @click="logout" class="logout-btn">
            <el-icon><CloseBold /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import {
  HomeFilled,
  UserFilled,
  Operation,
  List,
  Setting,
  CloseBold,
} from '@element-plus/icons-vue'
import {jwtDecode} from 'jwt-decode'

const router = useRouter()
const name = getName()

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}


// 获取当前登录用户的信息
function getUserInfo() {
  const token = localStorage.getItem('token')
  if (!token) return null
  
  try {
    return jwtDecode(token)
  } catch (e) {
    return null
  }
}

function getName() {
  const user = getUserInfo()
  return user?.name || '未知用户'
}
</script>

<style scoped>
.admin-container {
  width: 100vw;
  height: 100vh;
}
.aside {
  background: #fff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
}
.admin-logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #eee;
}
.logo-icon {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #32d699;
  color: #fff;
  font-size: 22px;
  font-weight: bold;
  display: grid;
  place-items: center;
}
.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}
.menu {
  border-right: none;
  margin-top: 10px;
}
.header {
  height: 70px !important;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
}
.header-left {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-name {
  font-size: 15px;
  color: #333;
}
.logout-btn {
  color: #666;
}
.logout-btn:hover {
  color: #32d699;
}
.main {
  background: #f5f7fa;
  padding: 30px;
}
.card-item {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  text-align: center;
}
.card-title {
  font-size: 15px;
  color: #666;
  margin-bottom: 12px;
}
.card-num {
  font-size: 28px;
  font-weight: 700;
  color: #32d699;
}
</style>