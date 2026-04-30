<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Trophy, Document, ChatDotRound, Management, Bell, Calendar, DataLine } from '@element-plus/icons-vue'
import request from '@/utils/request'

const stats = ref({
    users: 0,
    activeCompetitions: 0,
    registrations: 0,
    pendingAppeals: 0,
    pendingLeaders: 0
})

const announcements = ref<any[]>([])
const loading = ref(false)

const loadStats = async () => {
    loading.value = true
    try {
        const [statsRes, annoRes] = await Promise.all([
            request.get('/dashboard/stats'),
            request.get('/announcement/page', { params: { pageNum: 1, pageSize: 5 } })
        ]) as any

        if (statsRes.code === 200) {
            const d = statsRes.data
            stats.value.users = d.userCount
            stats.value.activeCompetitions = d.activeCompetitionCount
            stats.value.registrations = d.registrationCount
            stats.value.pendingAppeals = d.pendingAppealCount
            stats.value.pendingLeaders = d.pendingLeaderCount
        }
        if (annoRes.code === 200) {
            announcements.value = annoRes.data.records || []
        }
    } finally {
        loading.value = false
    }
}

onMounted(loadStats)
</script>

<template>
  <div class="dashboard-container">
    <div class="page-header">
        <div class="welcome-text">
            <h2>管理控制台</h2>
            <p>今天是 {{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}</p>
        </div>
    </div>

    <!-- Stats Grid -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card blue pointer" @click="$router.push('/admin/users')">
          <div class="stat-info">
            <div class="stat-label">总用户人数</div>
            <div class="stat-value">{{ stats.users }}</div>
          </div>
          <el-icon class="stat-icon"><User /></el-icon>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card green pointer" @click="$router.push('/admin/competitions')">
          <div class="stat-info">
            <div class="stat-label">正在进行竞赛</div>
            <div class="stat-value">{{ stats.activeCompetitions }}</div>
          </div>
          <el-icon class="stat-icon"><Trophy /></el-icon>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card orange pointer" @click="$router.push('/admin/teams')">
          <div class="stat-info">
            <div class="stat-label">全校报名总次</div>
            <div class="stat-value">{{ stats.registrations }}</div>
          </div>
          <el-icon class="stat-icon"><Document /></el-icon>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card purple pointer" @click="$router.push('/admin/appeals')">
          <div class="stat-info">
            <div class="stat-label">待处理申诉</div>
            <div class="stat-value">{{ stats.pendingAppeals }}</div>
          </div>
          <el-icon class="stat-icon"><Management /></el-icon>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="16">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 最新公告预览</span>
              <el-button link type="primary" @click="$router.push('/admin/announcements')">管理</el-button>
            </div>
          </template>
          <el-table :data="announcements" stripe style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="createTime" label="发布时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待处理业务</span>
            </div>
          </template>
          <div class="todo-list">
            <div class="todo-item" @click="$router.push('/admin/teachers')">
              <div class="todo-title">领队教师申请</div>
              <el-badge :value="stats.pendingLeaders" :hidden="stats.pendingLeaders === 0" />
            </div>
            <el-divider />
            <div class="todo-item" @click="$router.push('/admin/appeals')">
              <div class="todo-title">学生成绩申诉</div>
              <el-badge :value="stats.pendingAppeals" :hidden="stats.pendingAppeals === 0" type="danger" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container { padding: 0; }
.page-header { margin-bottom: 24px; }
.welcome-text h2 { margin: 0; font-size: 24px; color: #1e293b; }
.welcome-text p { margin: 4px 0 0; color: #64748b; font-size: 14px; }

.stats-row { margin-bottom: 24px; }
.stat-card {
    padding: 24px;
    border-radius: 16px;
    color: #fff;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;
    cursor: default;
}
.stat-card:hover { transform: translateY(-5px); }

.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.green { background: linear-gradient(135deg, #10b981, #059669); }
.orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.stat-label { font-size: 14px; opacity: 0.9; margin-bottom: 8px; font-weight: 500; }
.stat-value { font-size: 32px; font-weight: 800; }
.stat-icon { font-size: 48px; opacity: 0.2; }

.list-card, .todo-card { border-radius: 16px; border: 1px solid #f1f5f9; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.card-header .el-icon { margin-right: 8px; vertical-align: middle; }

.todo-list { padding: 10px 0; }
.todo-item { 
    display: flex; justify-content: space-between; align-items: center; 
    padding: 12px; border-radius: 8px; cursor: pointer; transition: background 0.2s;
}
.todo-item:hover { background-color: #f8fafc; }
.todo-title { font-size: 15px; color: #334155; }
</style>
