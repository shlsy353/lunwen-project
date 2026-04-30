<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <House />
        </el-icon>
        <span>个人仪表盘</span>
      </div>
    </div>

    <!-- Stats Row -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-card glass-blue clickable" @click="$router.push('/student/registrations')">
          <div class="stat-content">
            <div class="stat-label">参与竞赛</div>
            <div class="stat-value">{{ stats.registrations }}</div>
          </div>
          <el-icon class="stat-bg-icon">
            <Trophy />
          </el-icon>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card glass-green clickable" @click="$router.push('/student/award-situation')">
          <div class="stat-content">
            <div class="stat-label">所获奖励</div>
            <div class="stat-value">{{ stats.awards }}</div>
          </div>
          <el-icon class="stat-bg-icon">
            <Medal />
          </el-icon>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card glass-purple clickable" @click="$router.push('/student/scores')">
          <div class="stat-content">
            <div class="stat-label">综合得分</div>
            <div class="stat-value">{{ stats.avgScore?.toFixed(1) || 0 }}</div>
          </div>
          <el-icon class="stat-bg-icon">
            <TrendCharts />
          </el-icon>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px;">
      <!-- Active Competitions -->
      <el-col :md="16">
        <el-card class="premium-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title"><el-icon>
                  <Collection />
                </el-icon> 进行中的竞赛</span>
              <el-button link type="primary" @click="$router.push('/student/competitions')">查看全部</el-button>
            </div>
          </template>
          <div v-if="activeCompetitions.length === 0" class="empty-state">
            <el-empty description="暂无正在进行的竞赛" />
          </div>
          <div v-else class="comp-list">
            <div v-for="comp in activeCompetitions" :key="comp.id" class="comp-item"
              @click="$router.push('/competition/' + comp.id)">
              <div class="comp-info">
                <div class="comp-name">{{ comp.name }}</div>
                <div class="comp-meta">
                  <el-tag size="small" effect="plain">{{ comp.typeName }}</el-tag>
                  <span class="comp-date">截止：{{ comp.endTime || '长期' }}</span>
                </div>
              </div>
              <el-button type="primary" round size="small"
                @click.stop="$router.push('/competition/' + comp.id)">立即参加</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Recent Awards or User Profile Info -->
      <el-col :md="8">
        <el-card class="premium-card welcome-card" shadow="hover">
          <div class="welcome-section">
            <el-avatar :size="80" class="welcome-avatar">{{ (user.name || user.username || 'S')[0] }}</el-avatar>
            <h3 class="welcome-title">欢迎回来, {{ user.name || user.username }}!</h3>
            <p class="welcome-subtitle">{{ user.studentId ? '学号: ' + user.studentId : '正在追梦的竞赛选手' }}</p>
            <div class="action-grid">
              <div class="action-item" @click="$router.push('/student/registrations')">
                <el-icon>
                  <Tickets />
                </el-icon>
                <span>我的报名</span>
              </div>
              <div class="action-item" @click="$router.push('/student/scores')">
                <el-icon>
                  <Edit />
                </el-icon>
                <span>竞赛评分</span>
              </div>
              <div class="action-item" @click="$router.push('/student/profile')">
                <el-icon>
                  <Avatar />
                </el-icon>
                <span>个人中心</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="premium-card notification-card" shadow="hover" style="margin-top: 20px">
          <template #header>
            <span class="card-title"><el-icon>
                <Bell />
              </el-icon> 系统通知</span>
          </template>
          <div class="notice-list">
            <div v-for="n in announcements" :key="n.id" class="notice-item">
              <div class="notice-dot"></div>
              <div class="notice-content">
                <div class="notice-title">{{ n.title }}</div>
                <div class="notice-time">{{ n.createTime?.substring(5, 10) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { House, Trophy, Medal, TrendCharts, Collection, Bell, Tickets, Edit, Avatar } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = ref({ registrations: 0, awards: 0, avgScore: 0 })
const activeCompetitions = ref<any[]>([])
const announcements = ref<any[]>([])

const loadData = async () => {
  try {
    const [rRes, awRes, cRes, aRes, sRes] = await Promise.allSettled([
      request.get('/registration/page', { params: { pageSize: 1, studentId: user.id } }),
      request.get('/award/page', { params: { pageSize: 1, studentId: user.id } }),
      request.get('/competition/page', { params: { pageSize: 5, status: 1 } }),
      request.get('/announcement/page', { params: { pageSize: 5 } }),
      request.get('/score/page', { params: { studentId: user.id, pageSize: 100 } })
    ])

    if (rRes.status === 'fulfilled') stats.value.registrations = rRes.value.data?.total || 0
    if (awRes.status === 'fulfilled') stats.value.awards = awRes.value.data?.total || 0
    if (cRes.status === 'fulfilled') activeCompetitions.value = (cRes.value as any).data?.records || []
    if (aRes.status === 'fulfilled') announcements.value = (aRes.value as any).data?.records || []
    if (sRes.status === 'fulfilled') {
      const scores = (sRes.value as any).data?.records || []
      if (scores.length > 0) {
        const sum = scores.reduce((acc: number, curr: any) => acc + (Number(curr.score) || 0), 0)
        stats.value.avgScore = sum / scores.length
      } else {
        stats.value.avgScore = 0
      }
    }
  } catch (err) { console.error(err) }
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.title-icon {
  color: #3b82f6;
  font-size: 28px;
}

.stats-row {
  margin-bottom: 12px;
}

.stat-card {
  height: 120px;
  border-radius: 16px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.clickable {
  cursor: pointer;
}

.glass-blue {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.glass-green {
  background: linear-gradient(135deg, #10b981, #047857);
}

.glass-purple {
  background: linear-gradient(135deg, #8b5cf6, #5b21b6);
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  margin-top: 4px;
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 80px;
  opacity: 0.15;
  z-index: 1;
  transform: rotate(-15deg);
}

.premium-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: #334155;
  font-size: 16px;
}

.comp-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comp-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
  cursor: pointer;
}

.comp-item:hover {
  background: #f8fafc;
  border-color: #3b82f6;
  transform: scale(1.01);
}

.comp-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.comp-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comp-date {
  font-size: 12px;
  color: #64748b;
}

.welcome-section {
  text-align: center;
  padding: 10px 0;
}

.welcome-avatar {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  margin-bottom: 16px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.welcome-subtitle {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 12px;
  transition: background 0.2s;
  cursor: pointer;
}

.action-item:hover {
  background: #f1f5f9;
}

.action-item .el-icon {
  font-size: 24px;
  color: #3b82f6;
}

.action-item span {
  font-size: 12px;
  font-weight: 500;
  color: #475569;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.notice-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3b82f6;
}

.notice-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-title {
  font-size: 13px;
  color: #334155;
}

.notice-time {
  font-size: 12px;
  color: #94a3b8;
}
</style>
