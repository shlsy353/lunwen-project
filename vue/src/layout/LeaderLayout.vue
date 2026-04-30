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
      <el-container>
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
}

.sidebar {
  background: linear-gradient(180deg, #1a3a2a 0%, #0d2418 100%);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.2);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
  flex-shrink: 0;
}

.logo-collapsed {
  padding: 0;
  justify-content: center;
}

.logo-icon {
  font-size: 24px;
  color: #4ade80;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  letter-spacing: 1px;
}

.role-badge {
  text-align: center;
  padding: 6px 0;
  font-size: 11px;
  color: #4ade80;
  background: rgba(74, 222, 128, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
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
}

.sidebar-menu :deep(.el-menu-item) {
  color: #94a3b8;
  font-size: 13px;
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(74, 222, 128, 0.15) !important;
  color: #fff !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #16a34a, #166534) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.4);
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
  color: #94a3b8;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.2s;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: rgba(74, 222, 128, 0.1);
  color: #4ade80;
}

.main-header {
  background: #fff;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
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
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #16a34a, #166534);
  font-size: 14px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.arrow-icon {
  color: #909399;
  font-size: 14px;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
