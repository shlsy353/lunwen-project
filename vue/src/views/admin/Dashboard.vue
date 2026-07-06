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

    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="16">
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
      <el-col :xs="24" :lg="8">
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
.dashboard-container {
    padding: 0;
}

.page-header {
    margin-bottom: 26px;
}

.welcome-text h2 {
    margin: 0;
    color: #2f2a25;
    font-size: 28px;
    line-height: 1.15;
    font-weight: 800;
    letter-spacing: 0;
}

.welcome-text p {
    margin: 8px 0 0;
    color: #69645d;
    font-size: 14px;
    line-height: 1.5;
}

.stats-row {
    margin-bottom: 24px;
}

.stat-card {
    position: relative;
    min-height: 150px;
    overflow: hidden;
    padding: 24px 26px;
    border: 1px solid #ebe7df;
    border-radius: 24px;
    color: #3f3a34;
    background: rgba(255, 255, 255, 0.68);
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 20px 70px rgba(17, 17, 17, 0.035);
    backdrop-filter: blur(2px);
    transition: transform .18s ease, border-color .18s ease, background .18s ease;
    cursor: default;
}

.stat-card::before {
    content: "";
    position: absolute;
    left: 0;
    top: 18px;
    bottom: 18px;
    width: 4px;
    border-radius: 0 4px 4px 0;
    background: var(--accent);
}

.stat-card:hover {
    border-color: #d8d2c8;
    background: rgba(255, 255, 255, 0.86);
    transform: translateY(-3px);
}

.blue {
    --accent: #766a5d;
}

.green {
    --accent: #6b8f78;
}

.orange {
    --accent: #c28a43;
}

.purple {
    --accent: #8b7aa8;
}

.stat-label {
    color: #69645d;
    font-size: 14px;
    margin-bottom: 14px;
    font-weight: 700;
}

.stat-value {
    color: #2f2a25;
    font-size: 40px;
    line-height: 1;
    font-weight: 800;
}

.stat-icon {
    width: 58px;
    height: 58px;
    border: 1px solid #ebe7df;
    border-radius: 18px;
    color: var(--accent);
    background: rgba(255, 250, 241, 0.78);
    font-size: 28px;
    opacity: 1;
}

.content-row {
    margin-top: 24px;
}

.content-row :deep(.el-col) {
    margin-bottom: 20px;
}

.list-card,
.todo-card {
    overflow: hidden;
    border: 1px solid #ebe7df;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.68);
    box-shadow: 0 20px 70px rgba(17, 17, 17, 0.035);
    backdrop-filter: blur(2px);
}

.list-card :deep(.el-card__header),
.todo-card :deep(.el-card__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #ebe7df;
    background: rgba(255, 250, 241, 0.52);
}

.list-card :deep(.el-card__body),
.todo-card :deep(.el-card__body) {
    padding: 22px 24px 24px;
}

.list-card :deep(.el-table) {
    --el-table-border-color: #efebe4;
    --el-table-header-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-row-hover-bg-color: #fffaf1;
    color: #45413c;
    background: transparent;
}

.list-card :deep(.el-table th.el-table__cell) {
    color: #777168;
    background: transparent;
    font-weight: 700;
}

.list-card :deep(.el-table tr) {
    background: transparent;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #2f2a25;
    font-weight: 800;
}

.card-header span {
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.card-header .el-icon {
    color: #777168;
}

.card-header :deep(.el-button) {
    color: #4f4942;
    font-weight: 700;
}

.card-header :deep(.el-button:hover) {
    color: #68635b;
}

.todo-list {
    padding: 4px 0;
}

.todo-item { 
    display: flex;
    justify-content: space-between;
    align-items: center; 
    min-height: 64px;
    padding: 14px 16px;
    border: 1px solid transparent;
    border-radius: 14px;
    cursor: pointer;
    transition: background .16s ease, border-color .16s ease;
}

.todo-item:hover {
    border-color: #ebe7df;
    background: #fffaf1;
}

.todo-title {
    color: #34312d;
    font-size: 15px;
    font-weight: 700;
}

.todo-card :deep(.el-divider--horizontal) {
    margin: 8px 0;
    border-top-color: #ebe7df;
}

.todo-card :deep(.el-badge__content) {
    border: 0;
    background: #766a5d;
    font-weight: 800;
}

.todo-card :deep(.el-badge__content.is-fixed) {
    top: 6px;
}

@media (max-width: 900px) {
    .welcome-text h2 {
        font-size: 24px;
    }

    .stat-card {
        min-height: 132px;
        padding: 22px;
    }

    .stat-value {
        font-size: 34px;
    }
}
</style>
