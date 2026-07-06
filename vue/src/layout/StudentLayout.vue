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
  color: #111;
  background:
    radial-gradient(circle at 20% 8%, rgba(244, 225, 198, 0.54) 0%, rgba(244, 225, 198, 0.18) 24%, transparent 45%),
    radial-gradient(circle at 80% 10%, rgba(215, 225, 236, 0.50) 0%, rgba(215, 225, 236, 0.16) 28%, transparent 50%),
    linear-gradient(180deg, #fffdf8 0%, #fffaf1 48%, #fbfaf6 100%);
}

.main-header {
  border-bottom: 1px solid #ebe7df;
  background: rgba(255, 255, 255, 0.64);
  padding: 0;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: none;
  backdrop-filter: blur(10px);
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
  font-weight: 800;
  color: #111;
  white-space: nowrap;
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

.top-menu {
  border-bottom: none !important;
  flex: 1;
  background: transparent !important;
  overflow: hidden;
}

.top-menu :deep(.el-menu-item) {
  color: #69645d !important;
  font-size: 13px;
  padding: 0 10px;
  height: 42px;
  line-height: 42px;
  margin: 9px 3px;
  border-radius: 12px;
  border-bottom: none !important;
  font-weight: 600;
  transition: background .16s ease, color .16s ease, box-shadow .16s ease;
}

.top-menu :deep(.el-menu-item:hover) {
  color: #111 !important;
  background: rgba(17, 17, 17, 0.045) !important;
}

.top-menu :deep(.el-menu-item.is-active) {
  color: #fff !important;
  border-bottom: none !important;
  background: #111 !important;
  box-shadow: 0 10px 24px rgba(17, 17, 17, 0.12);
}

.user-actions {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #111;
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
  font-size: 13px;
  font-weight: 500;
}

.main-content {
  padding: 0;
  min-height: calc(100vh - 60px - 60px);
  background:
    linear-gradient(90deg, rgba(255, 253, 248, 0.82) 0%, rgba(255, 253, 248, 0.48) 36%, rgba(255, 253, 248, 0.82) 100%),
    linear-gradient(180deg, rgba(255, 253, 248, 0.08) 0%, rgba(255, 253, 248, 0.82) 100%);
}

.content-wrapper {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.main-footer {
  border-top: 1px solid #ebe7df;
  background: rgba(255, 255, 255, 0.66);
  color: #69645d;
  text-align: center;
  height: 60px;
  line-height: 60px;
  font-size: 13px;
}

@media (max-width: 900px) {
  .header-inner {
    padding: 0 14px;
  }

  .logo span,
  .username {
    display: none;
  }

  .content-wrapper {
    padding: 18px;
  }
}
</style>
