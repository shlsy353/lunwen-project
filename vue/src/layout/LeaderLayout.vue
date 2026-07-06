<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy, Document, Avatar, Edit, DataBoard, List, Tickets,
  ChatDotRound, User, Money, Memo, TrendCharts, Medal,
  ArrowLeft, ArrowRight, Setting
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const collapsed = ref(false)

const handleLogout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  ElMessage.success('已退出登录')
  router.push('/login')
}

const menuItems = [
  { path: '/leader/dashboard', icon: DataBoard, label: '首页' },
  { path: '/leader/info-dashboard', icon: Memo, label: '竞赛门户外网' },
  { path: '/leader/competitions-hall', icon: Trophy, label: '竞赛大厅' },
  { path: '/leader/applications', icon: Tickets, label: '报名审核' },
  { path: '/leader/expenses', icon: Money, label: '报销管理' },
  { path: '/leader/appeals', icon: ChatDotRound, label: '申诉处理' },
  { path: '/leader/profile', icon: Avatar, label: '个人中心' },
]
</script>

<template>
  <div class="layout-container">
    <el-container style="height: 100vh;">
      <!-- Sidebar -->
      <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
        <div class="logo" :class="{ 'logo-collapsed': collapsed }">
          <el-icon class="logo-icon">
            <Trophy />
          </el-icon>
          <span v-if="!collapsed" class="logo-text">竞赛管理系统</span>
        </div>
        <div v-if="!collapsed" class="role-badge">领队老师端</div>

        <el-menu :default-active="route.path" router class="sidebar-menu" :collapse="collapsed"
          :collapse-transition="false">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <template #title>{{ item.label }}</template>
          </el-menu-item>
        </el-menu>

        <div class="collapse-btn" @click="collapsed = !collapsed">
          <el-icon>
            <component :is="collapsed ? ArrowRight : ArrowLeft" />
          </el-icon>
        </div>
      </el-aside>

      <!-- Main Content -->
      <el-container class="main-body">
        <el-header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/leader/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta?.title || '当前页面' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="32" :src="user.avatar" class="user-avatar">
                  {{ (user.name || user.username || 'L')[0] }}
                </el-avatar>
                <span class="username">{{ user.name || user.username }}</span>
                <el-icon class="arrow-icon">
                  <Setting />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/leader/profile')">
                    <el-icon>
                      <Avatar />
                    </el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon>
                      <ArrowLeft />
                    </el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
  color: #111;
}

.sidebar {
  border-right: 1px solid #ebe7df;
  background: rgba(255, 255, 255, 0.66);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 12px 0 38px rgba(17, 17, 17, 0.035);
  backdrop-filter: blur(10px);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid #ebe7df;
  overflow: hidden;
  flex-shrink: 0;
}

.logo-collapsed {
  padding: 0;
  justify-content: center;
}

.logo-icon {
  width: 34px;
  height: 34px;
  border: 1px solid #ddd8cf;
  border-radius: 10px;
  color: #111;
  background: #fffaf1;
  font-size: 20px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 800;
  color: #111;
  white-space: nowrap;
  letter-spacing: 0;
}

.role-badge {
  text-align: center;
  padding: 6px 0;
  font-size: 11px;
  color: #8b8277;
  background: rgba(255, 250, 241, 0.72);
  border-bottom: 1px solid #f0ece4;
  letter-spacing: 3px;
  font-weight: 500;
  flex-shrink: 0;
}

.sidebar-menu {
  border: none;
  background: transparent;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #69645d;
  font-size: 13px;
  height: 46px;
  line-height: 46px;
  margin: 5px 8px;
  border-radius: 12px;
  font-weight: 600;
  transition: background .16s ease, color .16s ease, box-shadow .16s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(17, 17, 17, 0.045) !important;
  color: #111 !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #111 !important;
  color: #fff !important;
  box-shadow: 0 10px 24px rgba(17, 17, 17, 0.12);
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff;
}

.collapse-btn {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #777168;
  border-top: 1px solid #ebe7df;
  transition: background .16s ease, color .16s ease;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: rgba(17, 17, 17, 0.045);
  color: #111;
}

.main-header {
  background: rgba(255, 255, 255, 0.64);
  border-bottom: 1px solid #ebe7df;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: none;
  backdrop-filter: blur(10px);
  height: 60px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 7px 12px 7px 8px;
  border: 1px solid #e7e2d9;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.68);
  transition: background .16s ease, border-color .16s ease;
}

.user-info:hover {
  border-color: #d4d0c8;
  background: #fff;
}

.user-avatar {
  background: #111;
  font-size: 14px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #111;
  font-weight: 600;
}

.arrow-icon {
  color: #909399;
  font-size: 14px;
}

.main-content {
  background:
    linear-gradient(90deg, rgba(255, 253, 248, 0.82) 0%, rgba(255, 253, 248, 0.48) 36%, rgba(255, 253, 248, 0.82) 100%),
    linear-gradient(180deg, rgba(255, 253, 248, 0.08) 0%, rgba(255, 253, 248, 0.82) 100%);
  padding: 32px 36px;
  overflow-y: auto;
}

@media (max-width: 900px) {
  .main-content {
    padding: 24px 18px;
  }

  .main-header {
    padding: 0 16px;
  }
}
</style>
