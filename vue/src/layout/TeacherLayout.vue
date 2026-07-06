<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy, Document, Avatar, Edit, DataBoard, List, Tickets,
  ChatDotRound, User, Money, Memo, TrendCharts, Medal,
  ArrowLeft, ArrowRight, Setting, ScaleToOriginal
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 从本地存储同步用户信息
const user = JSON.parse(localStorage.getItem('user') || '{}')
const userRole = localStorage.getItem('role')
const isLeader = userRole === 'LEADER' || user.leaderStatus === 2 // 判断是否为领队老师角色
const isAdmin = userRole === 'ADMIN'

// 侧边栏折叠状态
const collapsed = ref(false)

/**
 * 退出登录处理：清除本地凭证并跳转
 */
const handleLogout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  ElMessage.success('已退出登录')
  router.push('/login')
}

/**
 * 统一的菜单配置列表
 * 所有功能入口都在此处维护。部分功能被标记为 'locked'，表示需要领队资格。
 */
const menuItems = [
  { path: '/teacher/dashboard', icon: DataBoard, label: '首页' },
  { path: '/teacher/info-dashboard', icon: Memo, label: '竞赛门户外网' },
  { path: '/teacher/competitions', icon: Trophy, label: '竞赛大厅' },
  { path: '/teacher/lead-students', icon: User, label: '带领学生管理', locked: true },
  { path: '/teacher/registrations', icon: Tickets, label: '我的报名', locked: true },
  { path: '/teacher/scoring', icon: ScaleToOriginal, label: '竞赛评分', locked: true },
  { path: '/teacher/award-situation', icon: Medal, label: '获奖情况', locked: true },
  { path: '/teacher/expenses', icon: Money, label: '报销管理', locked: true },
  { path: '/teacher/appeals', icon: ChatDotRound, label: '成绩申诉', locked: true },
  { path: '/teacher/profile', icon: Avatar, label: '个人中心' },
]
</script>

<template>
  <div class="layout-container">
    <el-container style="height: 100vh;">
      <!-- 侧边栏区域 -->
      <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
        <!-- 系统 Logo 展示区 -->
        <div class="logo" :class="{ 'logo-collapsed': collapsed }">
          <el-icon class="logo-icon">
            <Trophy />
          </el-icon>
          <span v-if="!collapsed" class="logo-text">竞赛管理系统</span>
        </div>
        <!-- 角色标签 -->
        <div v-if="!collapsed" class="role-badge">
          {{ isAdmin ? '管理端-教师视角' : (isLeader ? '教师端-领队老师' : '教师端') }}
        </div>

        <!-- 垂直导航菜单 -->
        <el-menu :default-active="route.path" router class="sidebar-menu" :collapse="collapsed"
          :collapse-transition="false">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path"
            :class="{ 'is-locked': item.locked && !isLeader }">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <template #title>
              <span>{{ item.label }}</span>
              <!-- 如果功能锁定且当前用户不是领队，显示锁图标 -->
              <span v-if="item.locked && !isLeader" class="lock-badge">🔒</span>
            </template>
          </el-menu-item>
        </el-menu>

        <!-- 侧边栏折叠切换按钮 -->
        <div class="collapse-btn" @click="collapsed = !collapsed">
          <el-icon>
            <component :is="collapsed ? ArrowRight : ArrowLeft" />
          </el-icon>
        </div>
      </el-aside>

      <!-- 右侧主内容区域 -->
      <el-container class="main-body">
        <!-- 顶部导航栏 -->
        <el-header class="main-header">
          <div class="header-left">
            <!-- 面包屑导航 -->
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/teacher/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ $route.meta?.title || '当前页面' }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <!-- 用户下拉菜单 -->
            <el-dropdown trigger="click">
              <div class="user-info">
                <el-avatar :size="32" :src="user.avatar" class="user-avatar">
                  {{ (user.name || user.username || 'T')[0] }}
                </el-avatar>
                <span class="username">{{ user.name || user.username }}</span>
                <el-icon class="arrow-icon">
                  <Setting />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/teacher/profile')">
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

        <!-- 业务组件渲染区 -->
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

.sidebar-menu :deep(.el-menu--collapse) {
  width: 64px;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 16px;
}

.sidebar-menu :deep(.el-menu-item.is-locked) {
  opacity: 0.62;
}

.sidebar-menu :deep(.el-menu-item.is-locked:hover) {
  background: rgba(17, 17, 17, 0.035) !important;
  color: #69645d !important;
}

.lock-badge {
  margin-left: auto;
  font-size: 11px;
  flex-shrink: 0;
  padding-right: 4px;
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
