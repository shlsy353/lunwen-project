<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <TrendCharts />
        </el-icon>
        <span>我的晋级历程</span>
      </div>
    </div>

    <!-- Celebration Header -->
    <div class="promotion-hero" v-if="tableData.length > 0">
      <div class="hero-content">
        <el-icon class="hero-icon">
          <Medal />
        </el-icon>
        <div class="hero-text">
          <h2>恭喜你，晋级了！</h2>
          <p>你已在 {{ tableData.length }} 个竞赛项目中获得晋级资格，继续保持！</p>
        </div>
      </div>
    </div>

    <el-card class="premium-card" shadow="never">
      <div v-if="loading" v-loading="loading" style="height: 300px"></div>
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="目前还没有晋级记录，少年仍需努力！">
          <el-button type="primary" round @click="$router.push('/student/competitions')">探索更多竞赛</el-button>
        </el-empty>
      </div>
      <div v-else class="promotion-grid">
        <div v-for="item in tableData" :key="item.id" class="promo-card">
          <div class="promo-header">
            <span class="promo-comp-id">竞赛 ID: #{{ item.competitionId }}</span>
            <el-tag type="primary" effect="dark" size="small">荣获晋级</el-tag>
          </div>
          <div class="promo-body">
            <div class="promo-comment">
              <div class="p-label"><el-icon>
                  <ChatLineSquare />
                </el-icon> 评审委员会评语：</div>
              <p class="p-text">{{ item.reviewComment || '该选手的表现极为出色，符合晋级标准。' }}</p>
            </div>
          </div>
          <div class="promo-footer">
            <span class="promo-date"><el-icon>
                <Calendar />
              </el-icon> 晋级日期：{{ item.createTime?.substring(0, 10) }}</span>
            <el-button link type="primary"
              @click="$router.push('/competition/' + item.competitionId)">查看竞赛详情</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <div class="promotion-footer">
      <el-icon class="footer-decor">
        <Collection />
      </el-icon>
      <span>晋级名单实时更新，详情请关注具体竞赛公告。</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { TrendCharts, Medal, ChatLineSquare, Calendar, Collection } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const user = JSON.parse(localStorage.getItem('user') || '{}')

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/registration/page', {
      params: { studentId: user.id, pageSize: 100 }
    }) as any
    if (res.code === 200) {
      tableData.value = res.data.records.filter((item: any) => item.status === 3)
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
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
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.title-icon {
  font-size: 24px;
  color: #3b82f6;
}

.promotion-hero {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 32px;
  color: #fff;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.hero-icon {
  font-size: 64px;
  color: #fbbf24;
  filter: drop-shadow(0 0 12px rgba(251, 191, 36, 0.3));
}

.hero-text h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
}

.hero-text p {
  margin: 0;
  opacity: 0.8;
  font-size: 15px;
}

.premium-card {
  border-radius: 16px;
  border: none;
  min-height: 40vh;
}

.promotion-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.promo-card {
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  background: #fff;
  transition: all 0.3s;
}

.promo-card:hover {
  transform: translateY(-5px);
  border-color: #3b82f6;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
}

.promo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.promo-comp-id {
  font-size: 13px;
  font-weight: 800;
  color: #94a3b8;
  font-family: monospace;
}

.promo-body {
  margin-bottom: 20px;
}

.promo-comment {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid #3b82f6;
}

.p-label {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.p-text {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
  font-style: italic;
}

.promo-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f1f5f9;
  padding-top: 16px;
}

.promo-date {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.promo-footer :deep(.el-button) {
  font-weight: 600;
}

.promotion-footer {
  margin-top: 40px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>
