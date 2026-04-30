<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor, User, Trophy, School, Bell, Document, Setting,
  ArrowLeft, ArrowRight, Avatar, DataAnalysis, List, Edit, Medal, TrendCharts, Tickets, Money, ChatDotRound, UserFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出管理员后台吗？', '安全提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.clear()
    ElMessage.success('安全退出')
    router.push('/login')
  }).catch(() => { })
}

const menuItems = [
  { path: '/admin/dashboard', icon: DataAnalysis, label: '数据大屏' },
  { path: '/admin/users', icon: User, label: '用户管治' },
  { path: '/admin/competition-types', icon: List, label: '竞赛类型' },
  { path: '/admin/competitions', icon: Trophy, label: '竞赛统筹' },
  { path: '/admin/scoring', icon: Edit, label: '评分管治' },
  { path: '/admin/teams', icon: User, label: '参赛名单' },
  { path: '/admin/awards', icon: Medal, label: '获奖成就' },
  { path: '/admin/teachers', icon: Avatar, label: '教师管治' },
  { path: '/admin/appeals', icon: ChatDotRound, label: '申诉管理' },
  { path: '/admin/announcements', icon: Bell, label: '全局公告' },
  { path: '/admin/teacher-center', icon: UserFilled, label: '教师端中心' },
]
</script>

<template>
  <div class="layout-container">
    <el-container style="height: 100vh;">
      <!-- Premium Dark Sidebar -->
      <el-aside :width="collapsed ? '64px' : '230px'" class="sidebar">
        <div class="logo">
          <el-icon class="logo-icon">
            <Monitor />
          </el-icon>
          <span v-if="!collapsed" class="logo-text">管理主控台</span>
        </div>
        <div v-if="!collapsed" class="role-tag">SYSTEM ADMIN</div>

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

      <!-- Main Container -->
      <el-container class="main-body">
        <el-header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>后台系统</el-breadcrumb-item>
              <el-breadcrumb-item>{{ route.meta.title || '控制台' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="32" :src="user.avatar" class="admin-avatar">
                  {{ (user.name || user.username || 'AD')[0] }}
                </el-avatar>
                <span class="username">{{ user.name || user.username || '管理员' }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/admin/profile')">
                    <el-icon>
                      <User />
                    </el-icon> 账户设置
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleLogout" class="logout-item" divided>
                    <el-icon>
                      <ArrowLeft />
                    </el-icon> 退出后台
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
  background: #f8fafc;
}

.sidebar {
  background: #0f172a;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  overflow: hidden;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 26px;
  color: #3b82f6;
}

.logo-text {
  color: #fff;
  font-weight: 800;
  font-size: 16px;
  letter-spacing: 1px;
  white-space: nowrap;
}

.role-tag {
  font-size: 10px;
  color: #3b82f6;
  font-weight: 900;
  letter-spacing: 4px;
  text-align: center;
  padding: 8px 0;
  background: rgba(59, 130, 246, 0.1);
  margin-bottom: 10px;
}

.sidebar-menu {
  border: none;
  background: transparent;
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #94a3b8;
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  border-radius: 12px;
  transition: all 0.2s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #3b82f6 !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff;
}

.collapse-btn {
  height: 48px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.main-header {
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 10px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f1f5f9;
}

.admin-avatar {
  background: #3b82f6;
  font-weight: 800;
  font-size: 12px;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.logout-item {
  color: #ef4444;
  font-weight: 600;
}

.main-content {
  padding: 30px;
  overflow-y: auto;
  background: #f8fafc;
}
</style>
