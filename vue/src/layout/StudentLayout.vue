<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy, Document, Avatar, Edit, DataBoard, List, Tickets,
  ChatDotRound, User, Money, Memo, TrendCharts, Medal,
  UserFilled, ArrowDown, House, Collection, Star
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const handleLogout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  ElMessage.success('已退出登录')
  router.push('/login')
}

const menuItems = [ 
  { path: '/student/dashboard', label: '首页' },
  { path: '/student/competition-types', label: '竞赛类型' },
  { path: '/student/competitions', label: '竞赛大厅' },
  { path: '/student/leaders', label: '我的领队' },
  { path: '/student/registrations', label: '我的报名' },
  { path: '/student/award-situation', label: '获奖成就' },
  { path: '/student/scores', label: '竞赛评分' },
  { path: '/student/expenses', label: '报销清单' },
  { path: '/student/appeals', label: '成绩申诉' },
]
</script>

<template>
  <div class="layout-container">
    <el-container>
      <!-- Top Navigation -->
      <el-header class="main-header">
        <div class="header-inner">
          <div class="logo">
            <el-icon class="logo-icon">
              <Trophy />
            </el-icon>
            <span>高校学科竞赛管理系统</span>
          </div>

          <el-menu mode="horizontal" router :default-active="route.path" class="top-menu" :ellipsis="false">
            <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
              {{ item.label }}
            </el-menu-item>
          </el-menu>

          <div class="user-actions">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="34" :src="user.avatar" class="user-avatar">
                  {{ (user.name || user.username || 'S')[0] }}
                </el-avatar>
                <span class="username">{{ user.name || user.username }}</span>
                <el-icon>
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/student/profile')">
                    <el-icon>
                      <UserFilled />
                    </el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>

      <el-footer class="main-footer">
        <div class="footer-content">
          <p>© 2024 高校学科竞赛管理系统 · All Rights Reserved</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-header {
  background: linear-gradient(135deg, #1a2a4a 0%, #0d1b36 100%);
  padding: 0;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.header-inner {
  max-width: 1600px;
  margin: 0 auto;
  height: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 22px;
  color: #60a5fa;
}

.top-menu {
  border-bottom: none !important;
  flex: 1;
  background: transparent !important;
  overflow: hidden;
}

.top-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 13px;
  padding: 0 10px;
  height: 60px;
  line-height: 60px;
  border-bottom: none !important;
  transition: color 0.2s;
}

.top-menu :deep(.el-menu-item:hover) {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.top-menu :deep(.el-menu-item.is-active) {
  color: #60a5fa !important;
  border-bottom: 2px solid #60a5fa !important;
  background: transparent !important;
}

.user-actions {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  font-size: 14px;
  font-weight: 600;
}

.username {
  font-size: 13px;
  font-weight: 500;
}

.main-content {
  padding: 0;
  min-height: calc(100vh - 60px - 60px);
}

.content-wrapper {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.main-footer {
  background: #1a2a4a;
  color: #64748b;
  text-align: center;
  height: 60px;
  line-height: 60px;
  font-size: 13px;
}
</style>
