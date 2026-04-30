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
.layout-container { min-height: 100vh; background-color: #f5f7fa; }
.main-header { background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05); padding: 0; height: 64px; position: sticky; top: 0; z-index: 1000; }
.header-inner { max-width: 1200px; margin: 0 auto; height: 100%; padding: 0 20px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 10px; font-size: 20px; font-weight: 600; color: #409EFF; min-width: 240px; cursor: pointer; }
.logo-icon { font-size: 24px; }
.top-menu { border-bottom: none !important; flex: 1; justify-content: center; }
.user-actions { min-width: 150px; display: flex; justify-content: flex-end; align-items: center; gap: 10px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; color: #606266; padding: 4px 8px; border-radius: 4px; transition: background-color 0.2s; }
.user-info:hover { background-color: #f0f2f5; }
.username { font-size: 14px; font-weight: 500; }
.main-content { padding: 0; }
.content-wrapper { min-height: calc(100vh - 64px - 60px); }
.main-footer { background-color: #2b3a4a; color: #909399; text-align: center; padding: 20px; margin-top: auto; }
</style>
