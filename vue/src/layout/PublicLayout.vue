<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Trophy, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const user = computed(() => {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
})

const handleLogout = () => {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    router.push('/login')
    ElMessage.success('已退出登录')
}
</script>

<template>
  <div class="layout-container">
    <el-container>
      <el-header class="main-header">
        <div class="header-inner">
          <div class="logo" @click="router.push('/')">
            <el-icon class="logo-icon"><Trophy /></el-icon>
            <span>大学生竞赛管理系统</span>
          </div>
          
          <el-menu
            mode="horizontal"
            router
            :default-active="$route.path"
            class="top-menu"
            :ellipsis="false"
          >
            <el-menu-item index="/dashboard">首页</el-menu-item>
            <el-menu-item index="/competitions">竞赛大厅</el-menu-item>
            <!-- Protected menu items only visible if logged in and STUDENT -->
            <template v-if="user && user.role === 'STUDENT'">
                <el-menu-item index="/student/registrations">我的报名</el-menu-item>
                <el-menu-item index="/student/works">作品提交</el-menu-item>
                <el-menu-item index="/student/scores">我的成绩</el-menu-item>
            </template>
          </el-menu>

          <div class="user-actions">
             <template v-if="user">
                 <el-dropdown trigger="click">
                  <span class="user-info">
                    <el-avatar :size="32" :src="user.avatar" :icon="UserFilled" />
                    <span class="username">{{ user.name || user.username }}</span>
                    <el-icon><ArrowDown /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="user.role === 'STUDENT'" @click="router.push('/student/profile')">个人中心</el-dropdown-item>
                      <el-dropdown-item v-if="user.role === 'ADMIN'" @click="router.push('/admin/dashboard')">管理后台</el-dropdown-item>
                      <el-dropdown-item v-if="user.role === 'TEACHER'" @click="router.push('/teacher/dashboard')">教师后台</el-dropdown-item>
                      <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
             </template>
             <template v-else>
                 <el-button type="primary" link @click="router.push('/login')">登录</el-button>
                 <el-button type="success" size="small" @click="router.push('/register')">注册</el-button>
             </template>
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
          <p>© 2024 College Competition Management System. All Rights Reserved.</p>
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
  box-shadow: none;
  backdrop-filter: blur(10px);
  padding: 0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 1000;
}
.header-inner { max-width: 1200px; margin: 0 auto; height: 100%; padding: 0 20px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 10px; font-size: 18px; font-weight: 800; color: #111; min-width: 240px; cursor: pointer; }
.logo-icon { width: 34px; height: 34px; border: 1px solid #ddd8cf; border-radius: 10px; color: #111; background: #fffaf1; font-size: 20px; }
.top-menu { border-bottom: none !important; flex: 1; justify-content: center; }
.top-menu :deep(.el-menu-item) { height: 42px; line-height: 42px; margin: 11px 3px; border-radius: 12px; color: #69645d !important; font-weight: 600; border-bottom: none !important; }
.top-menu :deep(.el-menu-item:hover) { color: #111 !important; background: rgba(17, 17, 17, 0.045) !important; }
.top-menu :deep(.el-menu-item.is-active) { color: #fff !important; background: #111 !important; box-shadow: 0 10px 24px rgba(17, 17, 17, 0.12); }
.user-actions { min-width: 150px; display: flex; justify-content: flex-end; align-items: center; gap: 10px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; color: #111; padding: 7px 12px 7px 8px; border: 1px solid #e7e2d9; border-radius: 999px; background: rgba(255, 255, 255, 0.68); transition: background .16s ease, border-color .16s ease; }
.user-info:hover { border-color: #d4d0c8; background: #fff; }
.username { font-size: 14px; font-weight: 600; color: #111; }
.main-content { padding: 0; background: linear-gradient(90deg, rgba(255, 253, 248, 0.82) 0%, rgba(255, 253, 248, 0.48) 36%, rgba(255, 253, 248, 0.82) 100%), linear-gradient(180deg, rgba(255, 253, 248, 0.08) 0%, rgba(255, 253, 248, 0.82) 100%); }
.content-wrapper { min-height: calc(100vh - 64px - 60px); }
.main-footer { border-top: 1px solid #ebe7df; background: rgba(255, 255, 255, 0.66); color: #69645d; text-align: center; padding: 20px; margin-top: auto; }

@media (max-width: 900px) {
  .header-inner { padding: 0 14px; }
  .logo { min-width: auto; }
  .logo span,
  .username { display: none; }
}
</style>
