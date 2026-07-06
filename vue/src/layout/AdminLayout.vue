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
      <!-- Sidebar -->
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
  color: #111;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  background:
    radial-gradient(circle at 20% 8%, rgba(244, 225, 198, 0.54) 0%, rgba(244, 225, 198, 0.18) 24%, transparent 45%),
    radial-gradient(circle at 80% 10%, rgba(215, 225, 236, 0.50) 0%, rgba(215, 225, 236, 0.16) 28%, transparent 50%),
    linear-gradient(180deg, #fffdf8 0%, #fffaf1 48%, #fbfaf6 100%);
}

.sidebar {
  position: relative;
  z-index: 2;
  border-right: 1px solid #ebe7df;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(10px);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 12px 0 38px rgba(17, 17, 17, 0.035);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid #ebe7df;
  overflow: hidden;
  flex-shrink: 0;
}

.logo-icon {
  width: 34px;
  height: 34px;
  border: 1px solid #ddd8cf;
  border-radius: 10px;
  color: #111;
  background: #fffaf1;
  font-size: 20px;
}

.logo-text {
  color: #111;
  font-weight: 800;
  font-size: 16px;
  letter-spacing: 0;
  white-space: nowrap;
}

.role-tag {
  font-size: 10px;
  color: #8b8277;
  font-weight: 900;
  letter-spacing: 3px;
  text-align: center;
  padding: 10px 0;
  background: rgba(255, 250, 241, 0.72);
  border-bottom: 1px solid #f0ece4;
  margin-bottom: 8px;
}

.sidebar-menu {
  border: none;
  background: transparent;
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #69645d;
  height: 46px;
  line-height: 46px;
  margin: 5px 0;
  border-radius: 12px;
  font-weight: 600;
  transition: background .16s ease, color .16s ease, box-shadow .16s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(17, 17, 17, 0.045);
  color: #111;
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
  height: 48px;
  border-top: 1px solid #ebe7df;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #777168;
  transition: background .16s ease, color .16s ease;
}

.collapse-btn:hover {
  background: rgba(17, 17, 17, 0.045);
  color: #111;
}

.main-body {
  position: relative;
}

.main-body::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(rgba(88, 83, 76, 0.10) 0.65px, transparent 0.65px),
    radial-gradient(rgba(88, 83, 76, 0.15) 1px, transparent 1px);
  background-position: 0 0, 11px 11px;
  background-size: 4px 4px, 22px 22px;
  pointer-events: none;
}

.main-header {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.62);
  border-bottom: 1px solid #ebe7df;
  backdrop-filter: blur(10px);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.main-header :deep(.el-breadcrumb__inner) {
  color: #78736c;
  font-weight: 600;
}

.main-header :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #111;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
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

.admin-avatar {
  background: #111;
  font-weight: 800;
  font-size: 12px;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #111;
}

.logout-item {
  color: #ef4444;
  font-weight: 600;
}

.main-content {
  position: relative;
  z-index: 1;
  padding: 32px 36px;
  overflow-y: auto;
  background:
    linear-gradient(90deg, rgba(255, 253, 248, 0.82) 0%, rgba(255, 253, 248, 0.48) 36%, rgba(255, 253, 248, 0.82) 100%),
    linear-gradient(180deg, rgba(255, 253, 248, 0.08) 0%, rgba(255, 253, 248, 0.82) 100%);
}

@media (max-width: 900px) {
  .main-content {
    padding: 24px 18px;
  }

  .main-header {
    padding: 0 16px;
  }

  .username {
    display: none;
  }
}
</style>
