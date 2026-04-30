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
      <el-container>
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
/* 样式模块保持原样 */
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: linear-gradient(180deg, #1a2a4a 0%, #0d1b36 100%);
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
  color: #60a5fa;
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
  color: #60a5fa;
  background: rgba(96, 165, 250, 0.1);
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
  background: rgba(96, 165, 250, 0.15) !important;
  color: #fff !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
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
  opacity: 0.7;
}

.sidebar-menu :deep(.el-menu-item.is-locked:hover) {
  background: rgba(148, 163, 184, 0.12) !important;
  color: #94a3b8 !important;
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
  color: #94a3b8;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.2s;
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: rgba(96, 165, 250, 0.1);
  color: #60a5fa;
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
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
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
